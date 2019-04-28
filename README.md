# Revolut

## Backend Test

Design and implement a RESTful API (including data model and the backing implementation)
for money transfers between accounts.
### Explicit requirements:
1. You can use Java, Scala or Kotlin.
2. Keep it simple and to the point (e.g. no need to implement any authentication).
3. Assume the API is invoked by multiple systems and services on behalf of end users.
4. You can use frameworks/libraries if you like (except Spring), but don't forget about
requirement #2 – keep it simple and avoid heavy frameworks.
5. The datastore should run in-memory for the sake of this test.
6. The final result should be executable as a standalone program (should not require
a pre-installed container/server).
7. Demonstrate with tests that the API works as expected.
### Implicit requirements:
1. The code produced by you is expected to be of high quality.
2. There are no detailed requirements, use common sense.
### Please put your work on github or bitbucket.

## API endpoints
### GET /api/account/{id}
Account info. Example:
```/api/account/1234567890```
Response:
```
{
    "id":"1234567890",
    "meta":"Rich Guy"
}
```

### GET /api/account/{id}/balance/
All balances for this account. Example:
```/api/account/1234567890/balance/```
Response:
```
[
    {"accountId":"1234567890","currency":"USD","value":1000},
    {"accountId":"1234567890","currency":"EUR","value":1100},
    {"accountId":"1234567890","currency":"GBP","value":1200}
]
```

### GET /api/account/{id}/balance/{currency}
Balance for the selected currency for this account. Example:
```/api/account/1234567890/balance/GBP```
Response:
```
{
    "accountId":"1234567890",
    "currency":"GBP",
    "value":1200
}
```

### POST /api/account/{id}/send/{currency}
receiverId - receiverId account ID
value - value to send

Money transfer endpoint. Example:
```/api/account/1234567890/send/GBP```
Post params:
```
{
    "receiverId": "1111111111",
    "value": "100"
}
```
Transaction is returned as a response.
Response:
```
{
    "id":"123456789011111111111556443043342",
    "senderId":"1234567890",
    "receiverId""1111111111",
    "currency":"GBP",
    "value":100,
    "status":"PENDING"
}
```

### GET /api/transaction/{id}
Transaction info.
The transaction status in previous example is PENDING. To make sure that the transaction was successful, we need to request its status via this endpoint.
Example:
```/api/transaction/123456789011111111111556443043342```
Response:
```
{
    "id":"123456789011111111111556443043342",
    "senderId":"1234567890",
    "receiverId""1111111111",
    "currency":"GBP",
    "value":100,
    "status":"SUCCESSFUL"
}
```