# ♕ BYU CS 240 Chess

This project demonstrates mastery of proper software design, client/server architecture, networking using HTTP and WebSocket, database persistence, unit testing, serialization, and security.

## 10k Architecture Overview

The application implements a multiplayer chess server and a command line chess client.

[![Sequence Diagram](10k-architecture.png)](https://sequencediagram.org/index.html#initialData=C4S2BsFMAIGEAtIGckCh0AcCGAnUBjEbAO2DnBElIEZVs8RCSzYKrgAmO3AorU6AGVIOAG4jUAEyzAsAIyxIYAERnzFkdKgrFIuaKlaUa0ALQA+ISPE4AXNABWAexDFoAcywBbTcLEizS1VZBSVbbVc9HGgnADNYiN19QzZSDkCrfztHFzdPH1Q-Gwzg9TDEqJj4iuSjdmoMopF7LywAaxgvJ3FC6wCLaFLQyHCdSriEseSm6NMBurT7AFcMaWAYOSdcSRTjTka+7NaO6C6emZK1YdHI-Qma6N6ss3nU4Gpl1ZkNrZwdhfeByy9hwyBA7mIT2KAyGGhuSWi9wuc0sAI49nyMG6ElQQA)

[![Server Design]()](https://sequencediagram.org/index.html?presentationMode=readOnly#initialData=IYYwLg9gTgBAwgGwJYFMB2YBQAHYUxIhK4YwDKKUAbpTngUSWDABLBoAmCtu+hx7ZhWqEUdPo0EwAIsDDAAgiBAoAzqswc5wAEbBVKGBx2ZM6MFACeq3ETQBzGAAYAdAE5T9qBACu2GADEaMBUljAASij2SKoWckgQaIEA7gAWSGBiiKikALQAfOSUNFAAXDAA2gAKAPJkACoAujAA9D4GUAA6aADeAETtlMEAtih95X0uU30ANDB9uOrJ0Bzj81Mus-Mow8BICGuT0wC+mMIlMAWs7FyU5f2DUCNjExtbC-qqy1Crr9NzfR2ewOf02pzYnG4sCu51E5SgURimSgAApItFYpRIgBHHxqMAASjOxVEl0KsnkShU6nK9hQYAAqh0UY9nkSKYplGpVGSjDpygAxJCcGBMygcmA6MKs4CjTAcqncy4wkkqcpoHwIBDEkQqXkKrk0mAgBFyFBi1GPDns7SK9S84zlBQcDiijoc+W2w083Iq3UocqqHzenVUUlXA3U1TlE0oM0KHxgVIo4CJ1LWz2U70O-kwZ2uhNJj2RpW+wqwtUwIMhiuGMvXSF3CKIzFQSJBhDMB4dZ6HN4A1NJ+oQADW6D7J0wENu0L9JXuAx7spe63+80HqWHY7QE7BocoysK2XM5QATE4nDAagBpbrdobL3fvDdb8egvqndAcTzePyBaDsHSMAADIQNESQBGkGRZMg5i8ucTbVHUTStAY6gJGgd6Lg+oxPgCixfCsT6nAh0KFNOUILjKuHvvhnzfL8q57hRB5znCMAIGBwooqB4E4nisRErW+pelGtL0haLJLqMNpZlGOaCsKroWhKUowNRYglva9a1uUKnaKG4blqqAYwFQwDIFomRVPRKwogRDFzPp8guA5KxCSZIlydyMampkhbJhuGZaT6hSOnmLp5mmxaiaWbGVtWUaGXq9YsWUIFcWg7aal22FPI+tHrmmr47u+4I3FCh5FNQTb3vlNFMc+xWjm+jUkcUrFXMeGBnheV63r0eW9oVfQvi1pVtWYnA-r4-gBF4KDoBl9i+MwkHpJkmDdcwc6IdIACiwH7fU+3NC0aGqBh3Rjdu+5kQ2M7lDd6BThVrHGf65ScStiY8WBq38fiHn+l5nJiTAdJgAFKbNduslg6WYW5kKIoBapYTPWgmYI9p8WmWjBnCRGsVGhwKDcP5aYw0O43w3aoV8uUkTDBANBRUWBkhVVulVsGSVE1caXlLxANqDlMD9KM6jAHShwi4mvPegAZpqH6vY2s4ffOEt9FLqgyyufTy8wiXcirBztTVmvwLBPUwOel43lhesG3L-0K6b6jm2rX4zX+AQIq6wFIjAADiy48ut0FbbbO1a4hofHWd9jLtdsMvaRvJCzAmPqzO8EmV9SLh1LPHFxHgOCcldbkiT0YQ-S0OY3T2ZXOFKMFtF2iShj6dY1zOmF+z6aE55xPeUakMKFqJdqCiLfyW3uaRBYqBsxZCBh8uMUTz6ePC0iAA8s8cvk1dVdnwexLPqjZZ2Osp1LawVBsjTlRr3Mdel-SP2oz+v5bC49Ztq9UdgNH+Ed-5TDflNb8mAvCzUCNgHwUBsDcHgH5Qws8UgbRgjkOO1VtZIQaMnVOwQ+5YV-gAOQKpNTOgs3rpUxnMCBowaENSOMxRhBdPrGkwbPFEcB+HLkroSc+48cb1ynlTZu2N6YKRgB3Ye6Mc59zka3feyjR4gwkfTXycZMgCN-sFOuCj8xb1GDvSRn9eGew0ALcijDyhCIMSgWed9cq-wAJKMU4e8ahtC-Hv3zoPK2C5vG+P7PMAJHC3iAM6keWOvUACMGDXEcEocuHxeFonLnYYbOJsC-ZzUsOTTiyQYAACkIDCgsYYAIOgECgBHDHfBPCiFVAZChFov804023FhNBwBSlQDgBATiUAcl9G8dIYid0s5ONUf09ALCFhNJGWMiZUyZlzLSu09iAArGpaABHVOFO4lAuIgbiNrrvcSUMZF9wXojRmiilJaPkD3JZm5xrqMXpogm8gbkyDrvcoxWTpDPO0kjRSIoT7dzUjMv5cUtbsXhUComtzJHlD8FZNxy4ADqGRUhVHWZQFEMy5iPC8dIOYQyNnjOgFChm4UGTYDxXUiUyRiUwDQCgCp9LaAD00XY8RDCNblDOSckRYt76sJQNkkasQ5DtCmYKqAqgoEuBgXs0J2t5WKsagCZVYBVUjXVZq8oL9oHxOtiA+2fUnaDQiVMk1ZqjXzAtVqmBvt4G-jml4YZNs4ywGANgNBhB4iJBwdHba7S9qHWOqdVoxh5nisesabgeB5550qpokAWaoAchzZikFdyjDk3pCgaeCALSqBzQPGFzYWbry1G6cUnNTF6vYqK0tIVyhkwptWmeEcG1dqbczVmhgN6cs7bvGxYYEp825MC-tFah01oCiVetzKFGTtbZvQFwBkW41RUumsY902UWDXgDxOsXaywmDWow3cC0hpQKsYJebdrf11tyV2T621WWAJm99n601dSSQ6sBzt-2PvmM+4DoG8Afp9pwIAA)

## Modules

The application has three modules.

- **Client**: The command line program used to play a game of chess over the network.
- **Server**: The command line program that listens for network requests from the client and manages users and games.
- **Shared**: Code that is used by both the client and the server. This includes the rules of chess and tracking the state of a game.

## Starter Code

As you create your chess application you will move through specific phases of development. This starts with implementing the moves of chess and finishes with sending game moves over the network between your client and server. You will start each phase by copying course provided [starter-code](starter-code/) for that phase into the source code of the project. Do not copy a phases' starter code before you are ready to begin work on that phase.

## IntelliJ Support

Open the project directory in IntelliJ in order to develop, run, and debug your code using an IDE.

## Maven Support

You can use the following commands to build, test, package, and run your code.

| Command                    | Description                                     |
| -------------------------- | ----------------------------------------------- |
| `mvn compile`              | Builds the code                                 |
| `mvn package`              | Run the tests and build an Uber jar file        |
| `mvn package -DskipTests`  | Build an Uber jar file                          |
| `mvn install`              | Installs the packages into the local repository |
| `mvn test`                 | Run all the tests                               |
| `mvn -pl shared test`      | Run all the shared tests                        |
| `mvn -pl client exec:java` | Build and run the client `Main`                 |
| `mvn -pl server exec:java` | Build and run the server `Main`                 |

These commands are configured by the `pom.xml` (Project Object Model) files. There is a POM file in the root of the project, and one in each of the modules. The root POM defines any global dependencies and references the module POM files.

## Running the program using Java

Once you have compiled your project into an uber jar, you can execute it with the following command.

```sh
java -jar client/target/client-jar-with-dependencies.jar

♕ 240 Chess Client: chess.ChessPiece@7852e922
```
