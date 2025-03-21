# Renew lock (keep alive)

Renews a lock a user may have on a return.

Calls to this API must be made by an authenticated and authorised user with an ADR enrolment in order for the data to be returned.

**URL**: `/user-answers/lock/:appaId/:periodKey/ttl`

**Method**: `PUT`

**URL Params**:

| Parameter Name | Type   | Description    | Notes                       |
|----------------|--------|----------------|-----------------------------|
| appaId         | String | The appa Id    |                             |
| periodKey      | String | The period key | YYAM (year, 'A,' month A-L) |

**Required Request Headers**:

| Header Name   | Header Value   | Description                                |
|---------------|----------------|--------------------------------------------|
| Authorization | Bearer {TOKEN} | A valid bearer token from the auth service |

***Example request:***

/alcohol-duty-returns/user-answers/lock/AP0000000001/24AF/ttl

## Responses

### Success response

**Code**: `200 OK`

**Response Body**

The response body returns a message indicating the lock renew was successful.

**Response Body Examples**

***An example release message:***

```
Lock refreshed
```

### Responses
**Code**: `401 UNAUTHORIZED`
This response can occur when a call is made by any consumer without an authorized session that has an ADR enrolment.

**Code**: `423 LOCKED`
This response can occur when the lock cannot be refreshed because another lock is already in place for the same return

**Code**: `500 INTERNAL_SERVER_ERROR`
This response can occur if the query to the database fails