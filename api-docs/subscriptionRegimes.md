# Get Valid Subscription Regimes

Returns the alcohol regimes under the subscription summary for the specified APPA ID.

Calls to this API must be made by an authenticated and authorised user with an ADR enrolment in order for the data to be
returned.

**URL**: `/alcohol-duty-returns/subscriptionSummary/:appaId`

**Method**: `GET`

**URL Params**

| Parameter Name | Type   | Description | Notes |
|----------------|--------|-------------|-------|
| appaId         | String | The appa Id |       |

**Required Request Headers**:

| Header Name   | Header Value   | Description                                |
|---------------|----------------|--------------------------------------------|
| Authorization | Bearer {TOKEN} | A valid bearer token from the auth service |

***Example request:***

/alcohol-duty-returns/subscriptionSummary/AP0000000001

## Responses

### Success response

**Code**: `200 OK`

**Response Body**

The response body returns the set of alcohol regimes (possible values are: Beer, Cider, Wine, Spirits,
OtherFermentedProduct). The set could be empty, but this should not happen in practice as all users should have at least
one alcohol regime approval.

If the subscription approval status is not Approved or Insolvent, a FORBIDDEN error is returned.
If any error occurs, the error status code (as a result) wraps the message and is passed downstream.

**Response Body Examples**

***Set of alcohol regimes returned:***

```json
[
  "Spirits",
  "Wine",
  "Cider",
  "OtherFermentedProduct",
  "Beer"
]
```

### Responses

**Code**: `401 UNAUTHORIZED`
This response can occur when a call is made by any consumer without an authorized session that has an ADR enrolment.

**Code**: `403 FORBIDDEN`
This response can occur if the subscription approval status is not Approved or Insolvent.

**Code**: `500 INTERNAL_SERVER_ERROR`
This response can occur if there is an error getting subscriptions.