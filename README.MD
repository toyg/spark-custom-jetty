Custom Jetty instance for Spark
=====================


This repository shows a basic __example__ of how the Jetty instance embedded in [Spark](http://sparkjava.com) can be customized.
You can lift most of this code as-is. It works as of version 2.8.0, and
I expect it should work with 2.7.x too but I didn't test it. I explained some of the theory behind this effort
[on my blog](http://blog.pythonaro.com/2019/02/how-to-customise-jetty-embedded-in.html).

I marked the places where you can add the most-commonly-requested features  
(SSL options, custom handlers/filters, threads, and websockets) with:

`/* ********* CUSTOMIZATION POINT ***************`

I would still suggest that you look at the original Spark source to understand how things are put together. 
I know you don't have time for that, but it actually took me less to read it and code up the solution, 
than to google aimlessly for "easy" hacks that never materialized; also, by the time you read this, this very solution 
might be out of date if copied verbatim.

If you like this...
======
... give me a shout at https://www.linkedin.com/in/glacava/, I could do with more paid work.


Credits
======
(c) 2019 Giacomo Lacava. Licensed under the Apache License, Version 2.0.

Contains quite a bit of code from Spark, which is (c) 2019 Per Wendel, 
used under the terms of the Apache License, Version 2.0.

Pull requests and corrections are welcome.
