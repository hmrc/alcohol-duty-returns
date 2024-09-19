# Submit return

Submits a return for the user for a specific period

Calls to this API must be made by an authenticated and authorised user with an ADR enrolment in order for the data to be returned.

**URL**: `/alcohol-duty-returns/producers/:appaId/returns/:periodKey`

**Method**: `POST`

**URL Params**:

| Parameter Name | Type   | Description    | Notes                       |
|----------------|--------|----------------|-----------------------------|
| appaId         | String | The appa Id    |                             |
| periodKey      | String | The period key | YYAM (year, 'A,' month A-L) |

**Required Request Headers**:

| Header Name   | Header Value   | Description                                |
|---------------|----------------|--------------------------------------------|
| Authorization | Bearer {TOKEN} | A valid bearer token from the auth service |

**Request Body**

The details of the submitted return.

All quantities, volumes and monetary amounts are to 2 decimal places, except litres of pure alcohol which is to 4 decimal places.

| Field Name                                                       | Description                                                        | Data Type    | Mandatory/Optional  | Notes                                                                                          |
|------------------------------------------------------------------|--------------------------------------------------------------------|--------------|---------------------|------------------------------------------------------------------------------------------------|
| dutyDeclared                                                     | The duty being declared section                                    | Object       | Mandatory           |                                                                                                |
| dutyDeclared.declared                                            | Whether duty was declared                                          | Boolean      | Mandatory           |                                                                                                |
| dutyDeclared.items                                               | The declared items                                                 | Array(Items) | Mandatory           |                                                                                                |
| dutyDeclared.quantityDeclared                                    | The quantity declared                                              | Object       | Mandatory           |                                                                                                |
| dutyDeclared.quantityDeclared.litres                             | The number of litres of the product                                | Numeric      | Mandatory           |                                                                                                |
| dutyDeclared.quantityDeclared.lpa                                | The number of litres of pure alcohol in the product                | Numeric      | Mandatory           |                                                                                                |
| dutyDeclared.dutyDue                                             | The duty due details                                               | Numeric      | Mandatory           |                                                                                                |
| dutyDeclared.dutyDue.taxCode                                     | The three digit tax code                                           | Numeric      | Mandatory           |                                                                                                |
| dutyDeclared.dutyDue.dutyRate                                    | The rate of duty                                                   | Numeric      | Mandatory           |                                                                                                |
| dutyDeclared.dutyDue.dutyDue                                     | The duty due                                                       | Numeric      | Mandatory           |                                                                                                |
| adjustments                                                      | The adjustments section                                            | Object       | Mandatory           |                                                                                                |
| adjustments.overDeclarationDeclared                              | Whether there was an over-declaration adjustment                   | Boolean      | Mandatory           |                                                                                                |
| adjustments.reasonForOverDeclaration                             | The reason for over-declaration                                    | String       | Optional            | Only required in certain circumstances when declared                                           |
| adjustments.overDeclarationProducts                              | The details of any over-declarations                               | Array(Items) | Mandatory           |                                                                                                |
| adjustments.overDeclarationProducts.returnPeriod                 | The period key of when it was submitted                            | String       | Mandatory           | YYAM (year, 'A,' month A-L)                                                                    |
| adjustments.overDeclarationProducts.adjustmentQuantity           | The quantity to be adjusted                                        | String       | Mandatory           |                                                                                                |  
| adjustments.overDeclarationProducts.adjustmentQuantity.litres    | The number of litres of the product to be adjusted                 | Numeric      | Mandatory           |                                                                                                |
| adjustments.overDeclarationProducts.adjustmentQuantity.lpa       | The number of litres of pure alcohol in the product to be adjusted | Numeric      | Mandatory           |                                                                                                |
| adjustments.overDeclarationProducts.dutyDue                      | The duty due details                                               | Object       | Mandatory           |                                                                                                |  
| adjustments.overDeclarationProducts.dutyDue.taxCode              | The three digit tax code                                           | Numeric      | Mandatory           |                                                                                                |
| adjustments.overDeclarationProducts.dutyDue.dutyRate             | The rate of duty                                                   | Numeric      | Mandatory           |                                                                                                |
| adjustments.overDeclarationProducts.dutyDue.dutyDue              | The duty due                                                       | Numeric      | Mandatory           | Positive if owing, negative if a refund                                                        |
| adjustments.underDeclarationDeclared                             | Whether there was an under-declaration adjustment                  | Boolean      | Mandatory           |                                                                                                |
| adjustments.reasonForUnderDeclaration                            | The reason for under-declaration                                   | String       | Optional            | Only required in certain circumstances when declared                                           |
| adjustments.underDeclarationProducts                             | The details of any under-declarations                              | Array(Items) | Mandatory           |                                                                                                |
| adjustments.underDeclarationProducts.returnPeriod                | The period key of when it was submitted                            | String       | Mandatory           | YYAM (year, 'A,' month A-L)                                                                    |
| adjustments.underDeclarationProducts.adjustmentQuantity          | The quantity to be adjusted                                        | String       | Mandatory           |                                                                                                |  
| adjustments.underDeclarationProducts.adjustmentQuantity.litres   | The number of litres of the product to be adjusted                 | Numeric      | Mandatory           |                                                                                                |
| adjustments.underDeclarationProducts.adjustmentQuantity.lpa      | The number of litres of pure alcohol in the product to be adjusted | Numeric      | Mandatory           |                                                                                                | 
| adjustments.underDeclarationProducts.dutyDue                     | The duty due details                                               | Object       | Mandatory           |                                                                                                |   
| adjustments.underDeclarationProducts.dutyDue.taxCode               The three digit tax code                                           | Numeric      | Mandatory    |                                                                                                |
| adjustments.underDeclarationProducts.dutyDue.dutyRate              The rate of duty                                                   | Numeric      | Mandatory    |                                                                                                |
| adjustments.underDeclarationProducts.dutyDue.dutyDue               The duty due                                                       | Numeric      | Mandatory    | Positive if owing, negative if a refund                                                        |
| adjustments.spoilProductsDeclared                                | Whether there was a spoilt products adjustment                     | Boolean      | Mandatory           |                                                                                                |
| adjustments.spoiltProducts                                       | The details of any spoilt products                                 | Array(Items) | Mandatory           |                                                                                                |
| adjustments.spoiltProducts.returnPeriod                          | The period key of when it was submitted                            | String       | Mandatory           | YYAM (year, 'A,' month A-L)                                                                    |
| adjustments.spoiltProducts.adjustmentQuantity                    | The quantity to be adjusted                                        | String       | Mandatory           |                                                                                                |  
| adjustments.spoiltProducts.adjustmentQuantity.litres             | The number of litres of the product to be adjusted                 | Numeric      | Mandatory           |                                                                                                |
| adjustments.spoiltProducts.adjustmentQuantity.lpa                | The number of litres of pure alcohol in the product to be adjusted | Numeric      | Mandatory           |                                                                                                |
| adjustments.spoiltProducts.dutyDue                               | The duty due details                                               | Object       | Mandatory           |                                                                                                |  
| adjustments.spoiltProducts.dutyDue.taxCode                       | The three digit tax code                                           | Numeric      | Mandatory           |                                                                                                |
| adjustments.spoiltProducts.dutyDue.dutyRate                      | The rate of duty                                                   | Numeric      | Mandatory           |                                                                                                |
| adjustments.spoiltProducts.dutyDue.dutyDue                       | The duty due                                                       | Numeric      | Mandatory           | Positive if owing, negative if a refund                                                        |
| adjustments.drawbackDeclared                                     | Whether there was a drawback adjustment                            | Boolean      | Mandatory           |                                                                                                |
| adjustments.drawbackProducts                                     | The details of any drawback products                               | Array(Items) | Mandatory           |                                                                                                |
| adjustments.drawbackProducts.returnPeriod                        | The period key of when it was submitted                            | String       | Mandatory           | YYAM (year, 'A,' month A-L)                                                                    |
| adjustments.drawbackProducts.adjustmentQuantity                  | The quantity to be adjusted                                        | String       | Mandatory           |                                                                                                |  
| adjustments.drawbackProducts.adjustmentQuantity.litres           | The number of litres of the product to be adjusted                 | Numeric      | Mandatory           |                                                                                                |
| adjustments.drawbackProducts.adjustmentQuantity.lpa              | The number of litres of pure alcohol in the product to be adjusted | Numeric      | Mandatory           |                                                                                                |
| adjustments.drawbackProducts.dutyDue                             | The duty due details                                               | Object       | Mandatory           |                                                                                                |  
| adjustments.drawbackProducts.dutyDue.taxCode                     | The three digit tax code                                           | Numeric      | Mandatory           |                                                                                                |
| adjustments.drawbackProducts.dutyDue.dutyRate                    | The rate of duty                                                   | Numeric      | Mandatory           |                                                                                                |
| adjustments.drawbackProducts.dutyDue.dutyDue                     | The duty due                                                       | Numeric      | Mandatory           | Positive if owing, negative if a refund                                                        |
| adjustments.repackagedDraughtDeclared                            | Whether there was a repackaged draught adjustment                  | Boolean      | Mandatory           |                                                                                                |
| adjustments.repackagedDraughtProducts                            | The details of any repackaged draught products                     | Array(Items) | Mandatory           |                                                                                                |
| adjustments.repackagedDraughtProducts.returnPeriod               | The period key of when it was submitted                            | String       | Mandatory           | YYAM (year, 'A,' month A-L)                                                                    |
| adjustments.repackagedDraughtProducts.originalTaxCode            | The original three digit tax code                                  | String       | Mandatory           |                                                                                                |
| adjustments.repackagedDraughtProducts.originalDutyRate           | The original rate of duty                                          | Numeric      | Mandatory           |                                                                                                |
| adjustments.repackagedDraughtProducts.newTaxCode                 | The new three digit tax code                                       | String       | Mandatory           |                                                                                                |
| adjustments.repackagedDraughtProducts.newDutyRate                | The new rate of duty                                               | Numeric      | Mandatory           |                                                                                                |
| adjustments.repackagedDraughtProducts.repackagedQuantity         | The quantity to be adjusted                                        | String       | Mandatory           |                                                                                                |  
| adjustments.repackagedDraughtProducts.repackagedQuantity.litres  | The number of litres of the product to be adjusted                 | Numeric      | Mandatory           |                                                                                                |
| adjustments.repackagedDraughtProducts.repackagedQuantity.lpa     | The number of litres of pure alcohol in the product to be adjusted | Numeric      | Mandatory           |                                                                                                |
| adjustments.repackagedDraughtProducts.dutyAdjustment             | The duty owed                                                      | Numeric      | Mandatory           | Positive if owing, negative if a refund (expected positive)                                    |  
| dutySuspended                                                    | The duty suspended section                                         | Object       | Mandatory           |                                                                                                |  
| dutySuspended.declared                                           | Whether there was duty suspended declared                          | Boolean      | Mandatory           |                                                                                                |
| dutySuspended.dutySuspendedProducts                              | The declared duty suspended products                               | Array(Items) | Mandatory           |                                                                                                |
| dutySuspended.dutySuspendedProducts.regime                       | The declared regime                                                | Enum         | Mandatory           | Beer, Cider, Wine, Spirits, OtherFermentedProduct                                              |
| dutySuspended.dutySuspendedProducts.suspendedQuantity            | The quantity to be adjusted                                        | String       | Mandatory           |                                                                                                |  
| dutySuspended.repackagedDraughtProducts.suspendedQuantity.litres | The number of litres of the product to be suspended                | Numeric      | Mandatory           |                                                                                                |
| dutySuspended.repackagedDraughtProducts.suspendedQuantity.lpa    | The number of litres of pure alcohol to be suspended               | Numeric      | Mandatory           |                                                                                                |
| spirits                                                          | The quarterly spirits declaration                                  | Object       | Mandatory           |                                                                                                |
| spirits.spiritsDeclared                                          | Whether any spirits were (needed to be) declared                   | Boolean      | Mandatory           |                                                                                                |
| spirits.spiritsProduced                                          | Details of declared spirits                                        | Object       | Mandatory           |                                                                                                |
| spirits.spiritsProduced.totalSpirits                             | The total declared spirits volume                                  | Numeric      | Mandatory           |                                                                                                |
| spirits.spiritsProduced.scotchWhiskey                            | The total volume of scotch whiskey                                 | Numeric      | Optional            |                                                                                                |
| spirits.spiritsProduced.irishWhisky                              | The total volume of irish whisky                                   | Numeric      | Optional            |                                                                                                |
| spirits.spiritsProduced.typesOfSpirit                            | The types of spirit produced                                       | Enum         | Mandatory           | Malt, Grain, NeutralAgricultural, NeutralIndustrial, Beer, CiderOrPerry, WineOrMadeWine, Other |
| spirits.spiritsProduced.otherSpiritTypeName                      | The name of the other type of spirit produced                      | String       | Optional            | Only present if Other type of spirit was declared                                              |                                             |
| spirits.spiritsProduced.hasOtherMaltedGrain                      | Whether another type of malted grain was used                      | Boolean      | Mandatory           |                                                                                                |                                             |
| spirits.spiritsProduced.grainsQuantities                         | Quantities of various grains                                       | Object       | Mandatory           |                                                                                                |                                             |
| spirits.spiritsProduced.grainsQuantities.maltedBarley            | Quantity of malted barley used                                     | Numeric      | Mandatory           |                                                                                                |                                             |
| spirits.spiritsProduced.grainsQuantities.otherMaltedGrain        | Quantity of other malted grain used                                | Numeric      | Mandatory           |                                                                                                |                                             |
| spirits.spiritsProduced.grainsQuantities.wheat                   | Quantity of wheat used                                             | Numeric      | Mandatory           |                                                                                                |                                             |
| spirits.spiritsProduced.grainsQuantities.maltedBarley            | Quantity of malted barley used                                     | Numeric      | Mandatory           |                                                                                                |                                             |
| spirits.spiritsProduced.grainsQuantities.maize                   | Quantity of maize used                                             | Numeric      | Mandatory           |                                                                                                |                                             |
| spirits.spiritsProduced.grainsQuantities.rye                     | Quantity of rye barley used                                        | Numeric      | Mandatory           |                                                                                                |                                             |
| spirits.spiritsProduced.grainsQuantities.unmaltedGrain           | Quantity of unmalted grain used                                    | Numeric      | Mandatory           |                                                                                                |                                             |
| spirits.spiritsProduced.otherMaltedGrainType                     | The name of the other malted grain type                            | String       | Optional            | Present if hasOtherMaltedGrain is true                                                         |                                             |
| spirits.spiritsProduced.ingredientsVolumes                       | Volumes of various ingredients                                     | Object       | Mandatory           |                                                                                                |                                             |
| spirits.spiritsProduced.ingredientsVolumes.ethylene              | Volume of ethlyene gas used                                        | Numeric      | Mandatory           |                                                                                                |                                             |
| spirits.spiritsProduced.ingredientsVolumes.molasses              | Volume of molasses used                                            | Numeric      | Mandatory           |                                                                                                |                                             |
| spirits.spiritsProduced.ingredientsVolumes.beer                  | Volume of beer used                                                | Numeric      | Mandatory           |                                                                                                |                                             |
| spirits.spiritsProduced.ingredientsVolumes.wine                  | Volume of wine used                                                | Numeric      | Mandatory           |                                                                                                |                                             |
| spirits.spiritsProduced.ingredientsVolumes.madeWine              | Volume of made wine used                                           | Numeric      | Mandatory           |                                                                                                |                                             |
| spirits.spiritsProduced.ingredientsVolumes.ciderOrPerry          | Volume of cider or perry used                                      | Numeric      | Mandatory           |                                                                                                |                                             |
| spirits.spiritsProduced.otherIngredient                          | Details of another ingredient                                      | Object       | Optional            |                                                                                                |                                             |
| spirits.spiritsProduced.otherIngredient.quantity                 | The quantity of this ingredient                                    | Numeric      | Mandatory           |                                                                                                |                                             |
| spirits.spiritsProduced.otherIngredient.unitOfMeasure            | The unit describing the quantity                                   | Enum         | Mandatory           | Tonnes, Litres                                                                                 |                                             |
| spirits.spiritsProduced.otherIngredient.ingredientName           | The name of this ingredient                                        | String       | Mandatory           |                                                                                                |
| totals                                                           | Subtotals and the grand total for declarations                     | Object       | Mandatory           |                                                                                                |
| totals.declaredDutyDue                                           | How much duty is due from declarations                             | Numeric      | Mandatory           | Positive                                                                                       |
| totals.overDeclaration                                           | How much duty is refundable from over-declaration                  | Numeric      | Mandatory           | Negative                                                                                       |
| totals.underDeclaration                                          | How much duty is due from under-declaration                        | Numeric      | Mandatory           | Positive                                                                                       |
| totals.spoiltProduct                                             | How much duty is refundable from spoilt products                   | Numeric      | Mandatory           | Negative                                                                                       |
| totals.drawback                                                  | How much duty is refundable from drawback                          | Numeric      | Mandatory           | Negative                                                                                       |
| totals.repackagedDraught                                         | How much duty is due from repackaged draught                       | Numeric      | Mandatory           | Positive if owing, negative if a refund (expected positive)                                    |
| totals.totalDutyDue                                              | The grand total to be paid or refunded                             | Numeric      | Mandatory           | Positive if owing, negative if a refund                                                        |

**Request Body Examples**

***An example submission request: ***

```json
{
  "dutyDeclared": {
    "declared": true,
    "dutyDeclaredItems": [
      {
        "quantityDeclared": {
          "litres": 1000.1,
          "lpa": 100.101
        },
        "dutyDue": {
          "taxCode": "331",
          "dutyRate": 1.27,
          "dutyDue": 127.12
        }
      },
      {
        "quantityDeclared": {
          "litres": 2000.21,
          "lpa": 200.2022
        },
        "dutyDue": {
          "taxCode": "332",
          "dutyRate": 1.57,
          "dutyDue": 314.31
        }
      }
    ]
  },
  "adjustments": {
    "overDeclarationDeclared": true,
    "reasonForOverDeclaration": "Submitted too much",
    "overDeclarationProducts": [
      {
        "returnPeriod": "24AD",
        "adjustmentQuantity": {
          "litres": 400.04,
          "lpa": 40.0404
        },
        "dutyDue": {
          "taxCode": "352",
          "dutyRate": 1.32,
          "dutyDue": -52.85
        }
      }
    ],
    "underDeclarationDeclared": true,
    "reasonForUnderDeclaration": "Submitted too little",
    "underDeclarationProducts": [
      {
        "returnPeriod": "24AC",
        "adjustmentQuantity": {
          "litres": 300.03,
          "lpa": 30.0303
        },
        "dutyDue": {
          "taxCode": "351",
          "dutyRate": 2.32,
          "dutyDue": 69.67
        }
      }
    ],
    "spoiltProductDeclared": true,
    "spoiltProducts": [
      {
        "returnPeriod": "24AE",
        "adjustmentQuantity": {
          "litres": 500.05,
          "lpa": 50.0505
        },
        "dutyDue": {
          "taxCode": "353",
          "dutyRate": 1.82,
          "dutyDue": -91.09
        }
      }
    ],
    "drawbackDeclared": true,
    "drawbackProducts": [
      {
        "returnPeriod": "24AF",
        "adjustmentQuantity": {
          "litres": 600.06,
          "lpa": 60.0606
        },
        "dutyDue": {
          "taxCode": "361",
          "dutyRate": 2.21,
          "dutyDue": -132.73
        }
      }
    ],
    "repackagedDraughtDeclared": true,
    "repackagedDraughtProducts": [
      {
        "returnPeriod": "24AG",
        "originalTaxCode": "371",
        "originalDutyRate": 0.27,
        "newTaxCode": "331",
        "newDutyRate": 1.27,
        "repackagedQuantity": {
          "litres": 700.07,
          "lpa": 70.0707
        },
        "dutyAdjustment": 70.07
      }
    ]
  },
  "dutySuspended": {
    "declared": true,
    "dutySuspendedProducts": [
      {
        "regime": "Beer",
        "suspendedQuantity": {
          "litres": 1010.11,
          "lpa": 101.1011
        }
      },
      {
        "regime": "Wine",
        "suspendedQuantity": {
          "litres": 2020.22,
          "lpa": 202.2022
        }
      },
      {
        "regime": "Cider",
        "suspendedQuantity": {
          "litres": 3030.33,
          "lpa": 303.3033
        }
      },
      {
        "regime": "Spirits",
        "suspendedQuantity": {
          "litres": 404.44,
          "lpa": 404.4044
        }
      },
      {
        "regime": "OtherFermentedProduct",
        "suspendedQuantity": {
          "litres": 505.55,
          "lpa": 505.5055
        }
      }
    ]
  },
  "spirits": {
    "spiritsDeclared": true,
    "spiritsProduced": {
      "spiritsVolumes": {
        "totalSpirits": 123.45,
        "scotchWhiskey": 234.56,
        "irishWhisky": 345.67
      },
      "typesOfSpirit": [
        "Malt",
        "Beer",
        "Other"
      ],
      "otherSpiritTypeName": "MaltyBeer",
      "hasOtherMaltedGrain": true,
      "grainsQuantities": {
        "maltedBarley": 10,
        "otherMaltedGrain": 11.11,
        "wheat": 22.22,
        "maize": 33.33,
        "rye": 44.44,
        "unmaltedGrain": 55.55
      },
      "otherMaltedGrainType": "Smarties",
      "ingredientsVolumes": {
        "ethylene": 10.1,
        "molasses": 20.2,
        "beer": 30.3,
        "wine": 40.4,
        "madeWine": 50.5,
        "ciderOrPerry": 60.6
      },
      "otherIngredient": {
        "quantity": 70.7,
        "unitOfMeasure": "Tonnes",
        "ingredientName": "Coco Pops"
      }
    }
  },
  "totals": {
    "declaredDutyDue": 441.53,
    "overDeclaration": -52.85,
    "underDeclaration": 69.67,
    "spoiltProduct": -91.09,
    "drawback": -132.73,
    "repackagedDraught": 70.07,
    "totalDutyDue": 304.6
  }
}
```

***An example nil return request: ***

```json
{
  "dutyDeclared": {
    "declared": false,
    "dutyDeclaredItems": []
  },
  "adjustments": {
    "overDeclarationDeclared": false,
    "overDeclarationProducts": [],
    "underDeclarationDeclared": false,
    "underDeclarationProducts": [],
    "spoiltProductDeclared": false,
    "spoiltProducts": [],
    "drawbackDeclared": false,
    "drawbackProducts": [],
    "repackagedDraughtDeclared": false,
    "repackagedDraughtProducts": []
  },
  "dutySuspended": {
    "declared": false,
    "dutySuspendedProducts": []
  },
  "spirits": {
    "spiritsDeclared": false
  },
  "totals": {
    "declaredDutyDue": 0,
    "overDeclaration": 0,
    "underDeclaration": 0,
    "spoiltProduct": 0,
    "drawback": 0,
    "repackagedDraught": 0,
    "totalDutyDue": 0
  }
}
```

## Responses

### Success response

**Code**: `201 CREATED`

**Response Body**

The response body returns the return for the period with the data that should be displayed for the past return in the frontend

| Field Name                                                 | Description                          | Data Type | Mandatory/Optional | Notes                                                                  |
|------------------------------------------------------------|--------------------------------------|-----------|--------------------|------------------------------------------------------------------------|
| processingDate                                             | The date of processing               | Timestamp | Mandatory          | YYYY-MM-DDThh:mm:ss.nnnnnnZ                                            |
| amount                                                     | The amount to pay                    | Numeric   | Mandatory          | Positive if owing, negative if a refund                                |
| chargeReference                                            | The charge reference                 | String    | Optional           | Present if the amount is non-zero                                      |
| paymentDueDate                                             | The date the payment is due          | Numeric   | Optional           | Present if there is an amount to pay                                   |

**Response Body Examples**

***An example submission response with money owing: ***

```json
{
  "processingDate": "2024-09-19T12:25:41.791317Z",
  "amount": 304.6,
  "chargeReference": "XA10816765686948",
  "paymentDueDate": "2024-07-25"
}
```

*** A nil return response: ***
```json
{
  "processingDate": "2024-09-19T12:57:46.524722Z",
  "amount": 0
}
```

### Responses
**Code**: `400 BAD_REQUEST`
If the downstream returned a BAD_REQUEST while performing calculations or submission

**Code**: `401 UNAUTHORIZED`
This response can occur when a call is made by any consumer without an authorized session that has an ADR enrolment.

**Code**: `404 NOT_FOUND`
The return was not found

**Code**: `500 INTERNAL_SERVER_ERROR`
This response can occur if the downstream returns another error