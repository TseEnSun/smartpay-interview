# Smartcloud instance prices API

**Important: Do NOT fork this repository if you want to submit a solution.**

Imagine we run our infrastructure on a fictional cloud provider, Smartcloud. As their machine instance prices fluctuate all the time, Smartcloud provides an API for us to retrieve their prices in real time. This helps us in managing our cost.

# Requirements

Implement an API for fetching and returning machine instance prices from Smartcloud.

```
GET /prices?kind=sc2-micro
{"kind":"sc2-micro","amount":0.42}, ... (omitted)
```

This project scaffold provides an end-to-end implementation of an API endpoint which returns some dummy data. You should try to follow the same code structure.

You should implement `SmartcloudPriceService` to call the [smartcloud](https://hub.docker.com/r/smartpayco/smartcloud) endpoint and return the price data. Note that the smartcloud service has usage quota and may return error occassionally to simulate unexpected errors. Please make sure your service is able to handle the constraint and errors gracefully.

You should also include a README file to document:-
1. Any assumptions you make
1. Any design decisions you make
1. Instruction on how to run your code

You should use git and make small commits with meaningful commit messages as you implement your solution.

# Setup

Follow the instruction at [smartcloud](https://hub.docker.com/r/smartpayco/smartcloud) to run the Docker container on your machine.

Clone or download this project onto your machine and run

```
$ sbt run
```

The API should be running on your port 8080.

# How to submit

Please push your code to a public repository and submit the link via email. Please do not fork this repository.



# Solution

## Assumption
The Smartcloud API is limited to 1000 requests per day. And there are 18 kinds of instance, it can only get one kind of instance price per API call. I asuumed that the instance price information is not require too frequently. So I will cache the price information in memory and only call the API when the cache is empty or expired.

## How to run

```shell
# run the smartcloud and Redis docker containers
docker-compose up -d

# run the API server
sbt run

```

## Design
### Plan

1. Make a note about the design
2. Integrate with smartcloud first with happy path
3. Improve and optimization

### Note

* The API is idempotent,then retrying is a perfectly valid solution. The only cost will be the limited quota.

* The stratige to save the API call to smartcloud.
    * Cache the result
        
        * What to Store
            * Instance kind and price from SmartCloud.
        
        * When to call
            * When cache is empty
            * When cache is expired
            
            If the cache expired time is too long, the price will be out of date. If the cache expired time is too short, the API call will be too frequent.
            Let say the cache expired time is 26 minutes, there are 18 kinds of instance, the maximun API call will be 18 times every 26 mintues. It is acceptable. (1440/26 * 18 ~= 997)
            
            
        * Where to store
            * Redis

            If the cache is stored in memory, the cache will be lost when the server restart. If the cache is stored in DB, the performance will be bad.
            If deploy with multiple containers, the in memory cache can not be shared, which may result inconsistent data or increate the API call to smartcloud.
            
    * Persist
        * Since the prices change by time, store the prices is like history log. I will ignore it here.

* SmartCloud is an external API for this project which will be integrated.
    
    * Reliability
        * According to the instruction, the smartcloud API will return error occassionally to simulate unexpected errors. The API should be able to handle the constraint and errors gracefully. I implement the smartcloud API client with retry strategy by http4s client middleware. The retry strategy is configurable. The default is 3 times.
    
    * Maintainability
        * Follow the fagless-final style, it is easy to replace the interpreter.
    
    * Scalability
        * Keep the server stateless, cache in redis, presist in DB if necessary.

* What if
    
    * Peak requests
        * I use CE Semaphore to limit the concurrent requests. The default is 1. Means that only one request can be processed at the same time. The other requests will be blocked until the previous request is finished.
    
    * Hit the requets limit
