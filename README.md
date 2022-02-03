# Functional Short URL API

This is REST API where you as user can shorten url and get shortened url stats (number of hits).

If user hits the url, which is short, application redirects to original url.

## Test and run

To run test for this application you have to run sbt test inside project folder.
```scala
sbt test
```

To use this application you have to run sbt inside project folder.
```scala
sbt run
```

## Usage (during application run)

You can use curl or other fancy tool (f.e. Postman) to send requests to application.

### API Endpoints
```bash
- POST /api/shorten

 Request: {"uri": "..."}, Response: {"short": "..."}
 
 # you have to attach plain JSON as a body f.e.
 # {
 #  "uri" : "google.com"
 # }

- GET /api/<short>

 Redirects to original URI and update hits number
 
- GET /api/<full>

 Response with Directory JSON
 
- GET /api/stats/

 Response with JSON of all created shorts

- GET /api/stats/<full>

 Response {"access_count": 123}

```

## Comments
This is not the final version of the application.

```
Things to do:
1. Improve logic inside endpoint GET /api/<short>
2. Implement Error Handling
```

## Tech stack
Scala 2.13, Http4s, cats, fs2, scalatest

## License
[MIT](https://choosealicense.com/licenses/mit/)