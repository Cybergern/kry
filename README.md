# Opinions

- Why did I implement the poller this way?

I tried to make it as simple as uncomplicated as possible while still trying to follow the Vert.x way of doing things with asynchronous calls. Ideally I would have complicated things a bit more with letting all of the polling go on at the same time rather than one at a time. A slight bit more documentation might have been good but I felt the structure was simple enough where the code was readable even without it.

- Why did I implement the database access this way?

I implemented the mapper method together with the Service POJO as a rudimentary way of getting database abstraction. I wasn't super comfortable in the beginning with the high reliance on JSON structures for extracting information. I at least tried using the JSON structures rather than raw JSON in string form. I did like the way it was easy to insert parameters using the WebClient

- Why did I implement the main verticle this way?

I never quite got to it, but I would have liked to move the router handling to a separate class and let the main verticle be a lot less cluttered, but I feel I managed to separate the concerns well into different functions without making any one of them too cluttered. I wanted to build a route with path /service/<servicename>/ but none of the functionality really required that. I feel the endpoint implementations are pretty good. 

- What was difficult?

The biggest difficulties were definitely with the testing. I never quite got my head around how to do this kind of testing asynchronously. I understand that you want your API to work that way, but when you're writing a test that will change the state of a service you need to wait for changes to finish and despite reading a lot of examples I never did understand how it was supposed to work. In the rest of the framework, Futures are a major point so I was looking for that but couldn't find any but the simplest examples where calls were not dependent on each other.

In general, this is my first time using such an asynchronously focused framework, so the most difficult part was trying to bend your head around that kind of thinking, especially without more elaborate examples of what a unit test or implementations are supposed to look like. 

- What was fun?

It was very fun to learn something new and working with a typed language after working untyped for the last year. I liked that the framework does a lot of things for you, so it was easy to define new routes and other methods once you got into the right way of thinking. 

- What would you have done if you had twice as much time?

I would have worked more on establishing a documentation standard and introduce an advanced logger instead of just printing to System.out all the time. In the long run one should probably introduce a database abstraction as well as changing out the database to something better for asynchronous access. I would have liked to have gotten test cases working fine, in that case I would have kept going on restricting access to services depending on the web client cookie. I was in the process of writing a test case that could fail so I had a base for developing that but couldn't get it to work unfortunately.

Questions:

- Is Vert.x your main framework for developing features in the backend?

- What is the standard for documentation in the system?

- How do you log things in the system?

- Do you build microservices? If so, how do they communicate with each other? Do the microservices own their own small databases or do you have a major one with a bigger API?

- What is your Way of Working?
