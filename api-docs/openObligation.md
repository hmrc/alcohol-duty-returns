# Get Open Obligation

Returns the details of an Open Obligation for the specified APPA ID and period key.

Calls to this API must be made by an authenticated and authorised user with an ADR enrolment in order for the data to be returned.

**URL**: `/alcohol-duty-returns/openObligation/:appaId/:periodKey`

**Method**: `GET`

**URL Params**

| Parameter Name | Type   | Description    | Notes                       |
|----------------|--------|----------------|-----------------------------|
| appaId         | String | The appa Id    |                             |
| periodKey      | String | The period key | YYAM (year, 'A,' month A-L) |

**Required Request Headers**:

| Header Name   | Header Value   | Description                                |
|---------------|----------------|--------------------------------------------|
| Authorization | Bearer {TOKEN} | A valid bearer token from the auth service |

***Example request:***

/alcohol-duty-returns/obligationDetails/AP0000000001/25AA

## Responses

### Success response

**Code**: `200 OK`

**Response Body**

The response body returns the open obligation (containing the following fields)

If there are no open obligations for the specified period, a NOT_FOUND error is returned by the upstream API.
If any error occurs, the error status code (as a result) wraps the message and is passed downstream.

| Field Name | Description                                        | Data Type | Mandatory/Optional | Notes                       |
|------------|----------------------------------------------------|-----------|--------------------|-----------------------------|
| status     | The current obligation status                      | Enum      | Mandatory          | Open, Fulfilled             |
| fromDate   | The date from which the period applies             | Date      | Mandatory          | YYYY-MM-DD                  |
| toDate     | The date to which the period applies               | Date      | Mandatory          | YYYY-MM-DD                  |
| dueDate    | The date the return is due to be filed and paid by | Date      | Mandatory          | YYYY-MM-DD                  |
| periodKey  | The period key of the obligation                   | String    | Mandatory          | YYAM (year, 'A', month A-L) |

**Response Body Examples**

***Open obligation returned:***

```json
{
  "status": "Open",
  "fromDate": "2024-08-01",
  "toDate": "2024-08-31",
  "dueDate": "2024-09-10",
  "periodKey": "24AH"
}
```

### Responses
**Code**: `401 UNAUTHORIZED`
This response can occur when a call is made by any consumer without an authorized session that has an ADR enrolment.

**Code**: `404 NOT_FOUND`
This response can occur if the APPA ID does not have an open obligation for the specified period.

**Code**: `500 INTERNAL_SERVER_ERROR`
This response can occur if there is an error getting obligations.