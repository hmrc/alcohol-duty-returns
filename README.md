# alcohol-duty-returns

This is the backend microservice that handles Returns related operations for Alcohol Duty Service. 

## API Endpoints

- [Create User Answers](api-docs/createUserAnswers.md): `POST /alcohol-duty-returns/cache/user-answers`
- [Get (User Answers) Cache](api-docs/getCache.md): `GET /alcohol-duty-returns/cache/get/:appaId/:periodKey`
- [Get Return For Period](api-docs/getReturn.md): `GET /alcohol-duty-returns/producers/:appaId/returns/:periodKey`
- [Get Obligation Details](api-docs/obligationDetails.md): `GET /alcohol-duty-returns/obligationDetails/:appaId`
- [Set (User Answers) Cache](api-docs/setCache.md): `PUT /alcohol-duty-returns/cache/set`
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
