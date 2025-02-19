# alcohol-duty-returns

This is the backend microservice that handles Returns related operations for Alcohol Duty Service. 

## API Endpoints

- [Create User Answers](api-docs/createUserAnswers.md): `POST /alcohol-duty-returns/user-answers`
- [Get User Answers](api-docs/getUserAnswers): `GET /alcohol-duty-returns/user-answers/:appaId/:periodKey`
- [Get Return For Period](api-docs/getReturn.md): `GET /alcohol-duty-returns/producers/:appaId/returns/:periodKey`
- [Get Obligation Details](api-docs/obligationDetails.md): `GET /alcohol-duty-returns/obligationDetails/:appaId`
- [Get Open Obligation](api-docs/openObligation.md): `GET /alcohol-duty-returns/openObligation/:appaId/:periodKey`
- [Get Valid Subscription Regimes](api-docs/subscriptionRegimes.md): `GET /alcohol-duty-returns/subscriptionSummary/:appaId`
- [Clear User Answers](api-docs/clearUserAnswers.md): `DELETE /alcohol-duty-returns/user-answers/:appaId/:periodKey`
- [Release Lock](api-docs/releaseLock.md): `DELETE /user-answers/lock/:appaId/:periodKey`
- [Renew Lock (Keep Alive)](api-docs/renewLock.md): `PUT /user-answers/lock/:appaId/:periodKey/ttl`
- [Set User Answers](api-docs/setUserAnswers): `PUT /alcohol-duty-returns/user-answers`
- [Submit Return](api-docs/submitReturn.md): `POST /alcohol-duty-returns/producers/:appaId/returns/:periodKey`

## Running the service

> `sbt run`

The service runs on port `16001` by default.

## Running tests

### Unit tests

> `sbt test`

### Integration tests

> `sbt it/test`

## Scalafmt and Scalastyle

To check if all the scala files in the project are formatted correctly:
> `sbt scalafmtCheckAll`

To format all the scala files in the project correctly:
> `sbt scalafmtAll`

To check if there are any scalastyle errors, warnings or infos:
> `sbt scalastyle`
> 
> ### All tests and checks

This is an sbt command alias specific to this project. It will run a scala format
check, run a scala style check, run unit tests, run integration tests and produce a coverage report:
> `sbt runAllChecks`

### License

This code is open source software licensed under
the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
