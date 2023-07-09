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


# Design Note

## Plan

1. Make a note about the design
2. Integrate with smartcloud first with happy path
3. Improve and optimization

## Note
* The API is idempotent,then retrying is a perfectly valid solution.
* The stratige to save the API call to smartcloud.
    * Cache the result
        * What to Store
            * Result from SmartCloud: Get the supported instances from API, and so do the prices.
        * When to call
            * Call by call
                * Call API when cache expired (TTL: 90 seconds) 
            * Call scheduled
                * Call API every 90 seconds (86400/90 = 960 < 1000)
        * Where to store
            * Redis
            * In-Mem: Atomic Reference        
    * Persist
        * Since the prices change by time, store the prices is like history log. I will ignore it here.
* SmartCloud is an external API for this project which will be integrated.
    * reliability
        * SmartpayCloud no response or response parse error
            * retry
            * return error
    * maintainability
        * Follow the fagless-final style, it is easy to replace the interpreter.
    * scalability
        * Keep the server stateless, cache in redis, presist in DB if necessary

* What if
    * Peak request
        * Lock the smartpay API call
        * rate limit the endpoint
    * Hit the requets limit
