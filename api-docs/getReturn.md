# Get Return (For Period)

Returns the return for the user for a specific period

Calls to this API must be made by an authenticated and authorised user with an ADR enrolment in order for the data to be returned.

**URL**: `/alcohol-duty-returns/producers/:appaId/returns/:periodKey`

**Method**: `GET`

**URL Params**:

| Parameter Name | Type   | Description    | Notes                       |
|----------------|--------|----------------|-----------------------------|
| appaId         | String | The appa Id    |                             |
| periodKey      | String | The period key | YYAM (year, 'A,' month A-L) |

**Required Request Headers**:

| Header Name   | Header Value   | Description                                |
|---------------|----------------|--------------------------------------------|
| Authorization | Bearer {TOKEN} | A valid bearer token from the auth service |

## Responses

### Success response

**Code**: `200 OK`

**Response Body**

The response body returns the return for the period with the data that should be displayed for the past return in the frontend

All quantities, volumes and monetary amounts are to 2 decimal places, except litres of pure alcohol which is to 4 decimal places.

| Field Name                                                 | Description                                        | Data Type    | Mandatory/Optional | Notes                                                                  |
|------------------------------------------------------------|----------------------------------------------------|--------------|--------------------|------------------------------------------------------------------------|
| identification                                             | The identification details of the return           | Object       | Mandatory          |                                                                        |
| identification.periodKey                                   | The period of the return                           | String       | Mandatory          | YYAM (year, 'A,' month A-L)                                            |
| identification.submittedTime                               | The time of the submission                         | Timestamp    | Mandatory          | YYYY-MM-DDThh:mm:ss.nnnnnnZ                                            |
| alcoholDeclared                                            | Details of the alcohol declared                    | Object       | Mandatory          |                                                                        |
| alcoholDeclared.alcoholDeclaredDetails                     | The declared duty by tax code                      | Array(Items) | Mandatory          |                                                                        |
| alcoholDeclared.alcoholDeclaredDetails.taxType             | The declared duty by tax code                      | String       | Mandatory          |                                                                        |
| alcoholDeclared.alcoholDeclaredDetails.litresOfPureAlcohol | The litres of pure alcohol declared                | Numeric      | Mandatory          |                                                                        |
| alcoholDeclared.alcoholDeclaredDetails.dutyRate            | The duty rate                                      | Numeric      | Mandatory          |                                                                        |
| alcoholDeclared.alcoholDeclaredDetails.dutyValue           | The total duty due for this item                   | Numeric      | Mandatory          |                                                                        |
| alcoholDeclared.total                                      | The total duty due across all items                | Numeric      | Mandatory          |                                                                        |
| adjustments                                                | The adjustment section                             | Object       | Mandatory          |                                                                        |
| adjustments.adjustmentDetails                              | The adjustment details                             | Array(Items) | Mandatory          |                                                                        |
| adjustments.adjustmentDetails.adjustmentTypeKey            | The type of adjustment                             | Enum         | Mandatory          | underdeclaration, overdeclaration, repackagedDraught, spolit, drawback |
| adjustments.adjustmentDetails.taxType                      | The user's id                                      | String       | Mandatory          |                                                                        |
| adjustments.adjustmentDetails.litresOfPureAlcohol          | The litres of pure alcohol to adjust               | String       | Mandatory          |                                                                        |
| adjustments.adjustmentDetails.dutyRate                     | The duty rate                                      | String       | Mandatory          |                                                                        |
| adjustments.adjustmentDetails.dutyValue                    | The value of the duty                              | String       | Mandatory          | Positive if owing, negative if a refund                                |
| adjustments.total                                          | The total of the adjustments                       | Numeric      | Mandatory          | Positive if owing, negative if a refund                                |
| totalDutyDue                                               | The total duty due section                         | Object       | Mandatory          |                                                                        |
| totalDutyDue.totalDue                                      | The total duty to pay or refund                    | Numeric      | Mandatory          | Positive if owing, negative if a refund                                |


**Response Body Examples**

***An example return: ***

```json
{
  "identification": {
    "periodKey": "24AF",
    "submittedTime": "2024-09-18T17:27:41.994849Z"
  },
  "alcoholDeclared": {
    "alcoholDeclaredDetails": [
      {
        "taxType": "301",
        "litresOfPureAlcohol": 12041,
        "dutyRate": 5.27,
        "dutyValue": 63456.07
      }
    ],
    "total": 63456.07
  },
  "adjustments": {
    "adjustmentDetails": [
      {
        "adjustmentTypeKey": "underdeclaration",
        "taxType": "301",
        "litresOfPureAlcohol": 989,
        "dutyRate": 5.27,
        "dutyValue": 5212.03
      },
      {
        "adjustmentTypeKey": "overdeclaration",
        "taxType": "302",
        "litresOfPureAlcohol": 100.58,
        "dutyRate": 3.56,
        "dutyValue": -358.07
      },
      {
        "adjustmentTypeKey": "repackagedDraught",
        "taxType": "304",
        "litresOfPureAlcohol": 100.81,
        "dutyRate": 12.76,
        "dutyValue": 1221.82
      },
      {
        "adjustmentTypeKey": "spoilt",
        "taxType": "305",
        "litresOfPureAlcohol": 1000.94,
        "dutyRate": 1.75,
        "dutyValue": -1751.65
      },
      {
        "adjustmentTypeKey": "drawback",
        "taxType": "309",
        "litresOfPureAlcohol": 1301.11,
        "dutyRate": 5.12,
        "dutyValue": -6661.69
      }
    ],
    "total": -2337.56
  },
  "totalDutyDue": {
    "totalDue": 61118.51
  }
}
```

*** A nil return (nothing declared case): ***
```json
{
  "identification": {
    "periodKey": "24AF",
    "submittedTime": "2024-09-18T17:50:46.867972Z"
  },
  "alcoholDeclared": {
    "total": 0
  },
  "adjustments": {
    "total": 0
  },
  "totalDutyDue": {
    "totalDue": 0
  }
}
```

### Responses
**Code**: `400 BAD_REQUEST`
If the downstream returned a BAD_REQUEST

**Code**: `401 UNAUTHORIZED`
This response can occur when a call is made by any consumer without an authorized session that has an ADR enrolment.

**Code**: `404 NOT_FOUND`
No cache entry was found for the appaId,periodKey pair

**Code**: `422 UUNPROCESSABLE_ENTITY`
The return could not be parsed

**Code**: `500 INTERNAL_SERVER_ERROR`
This response can occur if the downstream returns another error