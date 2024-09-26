# Release lock

Releases a lock a user may have on a return.

Calls to this API must be made by an authenticated and authorised user with an ADR enrolment in order for the data to be returned.

**URL**: `/cache/lock/:appaId/:periodKey`

**Method**: `DELETE`

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

/alcohol-duty-returns/cache/lock/AP0000000001/24AF

## Responses

### Success response

**Code**: `200 OK`

**Response Body**

The response body returns a message indicating the lock release was successful with the user id, appaId, and period key. This will also occur if the lock wasn't being held.

**Response Body Examples**

***An example release message:***

```
Locked release for user Int-01234567-89ab-cdef-fdec-ba9876543210 on return AP0000000001/24AF
```

### Responses
**Code**: `401 UNAUTHORIZED`
This response can occur when a call is made by any consumer without an authorized session that has an ADR enrolment.

**Code**: `500 INTERNAL_SERVER_ERROR`
This response can occur if the query to the database fails