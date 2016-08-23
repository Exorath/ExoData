# ExoData
Database management for games

Maven: [![](https://jitpack.io/v/Exorath/ExoData.svg)](https://jitpack.io/#Exorath/ExoData)

## How to use
###RxJava
ExoData uses [RxJava](https://github.com/ReactiveX/RxJava) for observables. ALL observables are cold, this means the operation will only occur once the observable gets subscribed. IO operations are done on the io threadpool of RxJava, you can always do Observable#toBlocking() to ensure you observe it synchronously.

### Getting a collection
```java
//Connects to MongoDB on the default port. This operation requires IO (I may make it an observable in the future)
DataAPI dataAPI = DataAPI.create();

//Let's get a database, we make this a blocking operation for simplicity. This means your thread will be blocked until the database is fetched.
ExoDatabase database = dataAPI.getDatabase("name").timeout(3, TimeUnit.SECONDS).toBlocking().first();

//Let's get a collection.
ExoCollection collection = database.getCollection("name").timeout(3, TimeUnit.SECONDS).toBlocking().first();

//Let's get our first Document. This operation does not do any IO.
ExoDocument document = collection.getDocument("testid");
```


### Fetching your first document
```java
//If the document does not exist yet, a document will be created and returned
Document bsonDoc = document.fetch().timeout(3, TimeUnit.SECONDS).toBlocking().first();
```
### Incrementing a field
```java
//Now I did not add a toBlocking, this means our thread will not wait for the operation to finish
document.inc("key", 123).subscribe();
```

### Decrementing a field if there is enough
```java
//Now I did not add a toBlocking, this means our thread will not wait for the operation to finish
document.decrementIfSufficient("key", 123).subscribe(updateResult -> {
  //We check if there was a modified document or a document was created. I may write another method that returns a boolean to simplify this.
  if(updateResult.getModifiedCount() > 0 || updateResult.getUpsertedId() != null)
    System.out.println("Decremented key");
  else
    System.out.println("Failed to decrement key, the value was not large enough");
});
```
