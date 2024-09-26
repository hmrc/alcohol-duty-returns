# Create User Answers

Creates the UserAnswers structure and stores in the cache (the structure is locked by the user).

Calls to this API must be made by an authenticated and authorised user with an ADR enrolment in order for the data to be returned.

**URL**: `/alcohol-duty-returns/cache/user-answers`

**Method**: `POST`

**Request Body**:

| Parameter Name     | Type   | Description        | Notes                       |
|--------------------|--------|--------------------|-----------------------------|
| returnId.appaId    | String | The appa Id        |                             |
| returnId.periodKey | String | The period key     | YYAM (year, 'A,' month A-L) |
| groupId            | String | The user's groupId |                             | 
| userId             | String | The user's id      |                             |

**Request Body Examples**

***An example cache creation request:***

```json
{
  "returnId": {
    "appaId": "AP0000000001",
    "periodKey": "24AF"
  },
  "groupId": "testGroupId-01234567-89ab-cdef-fdec-ba9876543210",
  "userId": "Int-01234567-89ab-cdef-fdec-ba9876543210"
}
```

**Required Request Headers**:

| Header Name   | Header Value   | Description                                |
|---------------|----------------|--------------------------------------------|
| Authorization | Bearer {TOKEN} | A valid bearer token from the auth service |

## Responses

### Success response

**Code**: `200 OK`

**Response Body**

The response body returns the user answers cache entry with regime and obligation details.

| Field Name                    | Description                                        | Data Type | Mandatory/Optional | Notes                                             |
|-------------------------------|----------------------------------------------------|-----------|--------------------|---------------------------------------------------|
| _id                           | The identification                                 | Object    | Mandatory          |                                                   |
| _id.appaId                    | The appaId                                         | String    | Mandatory          |                                                   |
| _id.periodKey                 | The period of the return                           | String    | Mandatory          | YYAM (year, 'A,' month A-L)                       |
| groupId                       | The user's groupId                                 | String    | Mandatory          |                                                   |
| internalId                    | The user's id                                      | String    | Mandatory          |                                                   |
| regimes                       | The regimes the user is subscribed                 | Set(Enum) | Mandatory          | Beer, Cider, Wine, Spirits, OtherFermentedProduct |
| data                          | The user answers data                              | Object    | Mandatory          | 'Free form'; also contains obligations            |
| data.obligationData           | The obligation data                                | Object    | Mandatory          |                                                   |
| data.obligationData.status    | The current obligation status                      | Enum      | Mandatory          | Open                                              | 
| data.obligationData.fromDate  | The date from which the period applies             | Date      | Mandatory          | YYYY-MM-DD                                        |
| data.obligationData.toDate    | The date to which the period applies               | Date      | Mandatory          | YYYY-MM-DD                                        |
| data.obligationData.dueDate   | The date the return is due to be filed and paid by | Date      | Mandatory          |                                                   |
| data.obligationData.periodKey | The periodKey of the obligation                    | Date      | Mandatory          | YYAM (year, A, month A-L)                         |
| data.lastUpdated              | The timestamp of the last update                   | Timestamp | Mandatory          | value inside $date.$numberLong                    |
| data.validUntil               | The timestamp of the validity expiry               | Timestamp | Mandatory          | value inside $date.$numberLong                    |


**Response Body Examples**

***An example created entry:***

```json
{
  "_id": {
    "appaId": "AP0000000001",
    "periodKey": "24AF"
  },
  "groupId": "testGroupId-01234567-89ab-cdef-fdec-ba9876543210",
  "internalId": "Int-01234567-89ab-cdef-fdec-ba9876543210",
  "regimes": [
    "Spirits",
    "Wine",
    "Cider",
    "OtherFermentedProduct",
    "Beer"
  ],
  "data": {
    "obligationData": {
      "status": "Open",
      "fromDate": "2024-06-01",
      "toDate": "2024-06-30",
      "dueDate": "2024-08-07",
      "periodKey": "24AF"
    }
  },
  "lastUpdated": {
    "$date": {
      "$numberLong": "1726578927221"
    }
  },
  "validUntil": {
    "$date": {
      "$numberLong": "1729170927221"
    }
  }
}
```

### Responses
**Code**: `401 UNAUTHORIZED`
This response can occur when a call is made by any consumer without an authorized session that has an ADR enrolment.

**Code**: `403 FORBIDDEN`
The user is in another state than Approved or Insolvent

**Code**: `404 NOT_FOUND`
No cache entry was found for the appaId,periodKey pair

**Code**: `410 GONE`
The return is not Open i.e. Fulfilled

**Code**: `422 UNPROCESSABLE_ENTITY`
The subscription summary or obligations couldn't be parsed

**Code**: `423 LOCKED`
The return is locked

**Code**: `500 INTERNAL_SERVER_ERROR`
This response can occur if errors getting obligations or subscriptions, or if the write to the database fails