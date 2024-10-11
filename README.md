# 🥃 DIPLOMATICO

> _You drank a lot of rum and forgot to update DB versions of your Room_.


_diplomatico_ provides a KSP processor and a Gradle plugin to check consistency between different Room DB versions.

If you forgot to update a database's version, because some entities changed, _diplomatico_ is capable to spot this oversight and alert you.

## 🎬 Scenario
Prerequisite: Android project using [Room](https://developer.android.com/training/data-storage/room) as local database;

---

Your database's `version = 1` is:

```
@Entity
internal data class Drink(
  @PrimaryKey val name: String,
)

@Database(
  entities = [Drink::class],
  version = 1,
)
internal abstract class DrinkDatabase : RoomDatabase()
```

Lat's say that eventually, you update your entities with a new field:

```
@Entity
internal data class Drink(
  @PrimaryKey val name: String,
  val receipe: String,
)
```

then you need to remember to raise up the database's `version = 2`.

## 🧙 Annotation + Gradle plugin
### Report generation
With _diplomatico_ you can annotate the database with `@HashingRoomDBVersion`.

Doing so, _diplomatico_'s KSP processor will generate a report for you in the build directory:
```
// generated report
<DB fully qualified name, hashing of DB's entities>
```

The report list an hash for each annotated database, where the hash is computed by using database's entities as function input.

### Report checking
Then, if you keep track of the generated report in your VCS, _diplomatico_'s Gradle plugin registers a Gradle task to be executed when the Android `assemble` task is executed.

This way everytime you build your app, `checkRoomVersions` task is executed to check if the new generated report matches the one stored in your project. If not, the build fails.

```
// versioned report
DrinkDatabase, hash_without_receipe_field

// generated report
DrinkDatabase, hash_with_receipe_field
```

Purpose of this checking phase is:
- warning developers that something have changed in one entity used by an annotated database (because hash value changed);
- so developers can update Room Database's version, if needed;
- then accept the new generated report (you can use `updateRoomVersions` Gradle task).

### Feature
- support for nested classes: KSP processor check for any class nested in entity classes.

## 🎮 Demo
Take a look at:
- `:demo` module for a [sample usage](https://github.com/alecarnevale/diplomatico/tree/master/demo);
- [Red PR](https://github.com/alecarnevale/diplomatico/pull/8) that is failing because a change has been introduce for an entity and so the versioned report is outdated;
- [Green PR](https://github.com/alecarnevale/diplomatico/pull/9) that is ready to be merged because after changing the entity it also updated the versioned report.
- [Red PR - nested classes](https://github.com/alecarnevale/diplomatico/pull/26) that is failing because a change has been introduce for a class that is nested in a field of an entity and so the versioned report is outdated;
- [Green PR - nested classes](https://github.com/alecarnevale/diplomatico/pull/27) that is ready to be merged because after changing a class that is nested in a field of an entity it also updated the versioned report.

## 🛠️ Installation

**WIP**

To start tracking the report in your project, you can use the `updateRoomVersions` Gradle task.
It will make a copy of the last generated report in the asset folder of your project.
Take a look at [this PR](https://github.com/alecarnevale/diplomatico/pull/7) for an example.

## 🙏 Thanks to
- Kotlin Compile Testing https://github.com/tschuchortdev/kotlin-compile-testing
