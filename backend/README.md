# MangaWatch:Backend

## Quick Start (TL;DR)

1. Install Java 21+ and Docker Desktop.
2. Run the Postgres container:
   docker run --name manga-postgres -e POSTGRES_USER=mangauser01 \
   -e POSTGRES_PASSWORD=secretPW -e POSTGRES_DB=mangadbpg \
   -p 5432:5432 -d postgres:16

3. Set environment variables for DB and Mangadex API.
4. Start the backend (via IDE or `mvn spring-boot:run`).
5. Initialize import:
   GET http://localhost:8080/admin/import/start
6. View imported manga:
   GET http://localhost:8080/api/manga

## A Primer/What You'll need

The Backend is the heart and soul of this application. It's a somewhat complicated process to get it all set up. Here's what you will need.

- Java installed(I use version 21.0.5). 
- An IDE you can use (I use Eclipse because Java but VSCode would work fine).
- Docker Desktop to run a container for the PostgreSql.
- A Mangadex account with access to their API (free, works well)
- I use Maven here, pom.xml will do the dependency management.
- Knowhow of a tool like Postman or Curl to send requests to Mangadex(I used Postman)

Note: There are a few old/extra files that I plan to eventually delete. They either start with 'Old' or are commented out entirely.

## Database Setup

This may get a bit tricky depending on what kind of errors you encounter but this is how it went for me/how I did it.

- Install Docker Desktop, do the initial setup. Refer to its walkthroughs, or just jump in and figure it out along the way.
- Run the command: ```docker run --name manga-postgres \
  -e POSTGRES_USER=mangauser01 \
  -e POSTGRES_PASSWORD=secretPW \
  -e POSTGRES_DB=mangadbpg \
  -p 5432:5432 \
  -d postgres:16``` (these can be anything you like so change accordingly, same for env variables)
- With the container created, run it in Docker desktop, and it SHOULD run without errors. 
- You can additionally run ```docker inspect manga-postgres``` in your console to see the config details.
- Now, open the backend directory and add these to your environment variables:-
  ```
  Variables :  Values
  DB_HOST   :  localhost
  DB_NAME   :  mangadbpg
  DB_USER   :  mangauser01
  DB_PASS   :  secretPW
  DB_POST   :  5432
  ```
- Attempt to run the application (MangawatchApplication.java) via IDE or `mvn spring-boot:run`.
- If everything went well your container should be connected to your backend application without errors.

### Troubleshooting: Timezone Errors
  If you get a timezone mismatch or LocalDateTime-related error. Add in the run config that your timezone is explicitly UTC/Etc, I added this in VM Arguments ```-Duser.timezone=Etc/UTC``` and it worked. It may differ depending on your VM or Run config.
  
## Using the Mangadex API

Now, naturally you want some entries in your application to test against right? And you would not want to add all entries manually.

- First, create a Mangadex account and log into it
- Then, go to this url: https://api.mangadex.org/docs/02-authentication/personal-clients/.
- Read it through, it will explain how to create a personal client and use the API. You may or may not need it but its good to have.
- After creating a personal client, note down and add these to the environment variables:-
```
Variables :  Values
MANGADEX_CLIENT_ID : personal-client-8fn736h-6767-931o-g874-kn48ira021s109-n792a54l 
MANGADEX_CLIENT_SECRET : E3knHpCViDJZ84UPXN6IlvsdcRm48x4XF
MANGADEX_PASSWORD : yourpassword
MANGADEX_USERNAME : yourusername
```
(these are all random, use your own secret, client id, username and password)
- Restart the backend here, or run it if its not already running, same for container.
- Now, before import ensure you have a stable internet connection, and do not stop the backend app or the docker container until the import is complete.
- The methods/functions for importing the DB (and the subsequent transformation) are already in the code. 
- Here is where you'll need curl or postman (anything really) to send requests to the mangadex database.
- Refer to the code in the com.mangawatch.importer directory if needed.
- Here's the endpoints you'll call/need here:-

  Start Import : ```POST http://localhost:8080/admin/import/start```
  
  Check Status : ```GET http://localhost:8080/admin/import/status```
  
  Resume Import : ```POST http://localhost:8080/admin/import/resume?cursor=2018-08-26T07:54:35``` (this stamp is here for reference/structure)
  
- The mangadex API allows only somewhere around 9000 - 10000 entries to be fetched at once (limiting). So your import will stop somehwere around that.
- Call the status endpoint, it will show you the timestamp of the last entry you called. We'll resume import entries from that timestamp.
- In the resume request, replace the timestamp ```POST resume?cursor=2018-08-26T07:54:35``` with ```POST resume?cursor=timestamp_from_status``` and then send it.
- Each batch returns ~9000 entries. Resume until you reach the desired count.
- If the import worked, you can call ```GET http://localhost:8080/api/manga``` to check for some imformation.
- The ```GET http://localhost:8080/admin/import/status``` endpoint will tell you the last import's timestamp and number of entries the db has.

## Bringing it Together

### 1. Start dependencies
- Ensure Docker Desktop is running.
- Ensure the `manga-postgres` container is running.
- Confirm all required environment variables are set.

### 2. Start the backend
- Run via your IDE (MangawatchApplication.java), or use:
  mvn spring-boot:run
- The backend starts on port 8080.

### 3. Verify everything works
- Check DB connectivity in logs (Flyway migrations should succeed).
- Check Manga endpoint:
  http://localhost:8080/api/manga
- Check import status:
  http://localhost:8080/admin/import/status

### Finally, start the frontend in your IDE to see that the db populates the pages.
