scalaVersion := "3.3.1"

name := "hello-world"

version := "0.1"

// FS2 parce que c'est beaucoup plus expliqué au niveau de la doc cf (notre exposé sur les streams)
val fs2Version = "3.9.2"

// Je fais un test, dont mind me
// https://http4s.org/
// Askip c'est good avec fs2 cf premiere page
val http4sVersion = "0.23.26"

// ??????????
val catsEffectVersion = "3.5.2"

val DoobieVersion = "1.0.0-RC1"
val NewTypeVersion = "0.4.4"

// Je rajoute un max de dependances que je trouve dans les tutos puis on fera le tri après
libraryDependencies ++= Seq(

    "org.http4s" %% "http4s-circe" % "0.23.27",
    "io.circe" %% "circe-generic" % "0.14.6",  
    "io.circe" %% "circe-parser" % "0.14.6",

    // pour logger 
    "org.slf4j" % "slf4j-simple" % "2.0.9",

    // module de base de scala
    "org.scala-lang.modules" %% "scala-parser-combinators" % "2.3.0",

    // j'ai pas fait le tri, j'ai tout pris
    "org.http4s" %% "http4s-ember-client" % http4sVersion,
    "org.http4s" %% "http4s-ember-server" % http4sVersion,
    "org.http4s" %% "http4s-dsl" % http4sVersion,
    "org.http4s" %% "http4s-core" % http4sVersion,
    "org.http4s" %% "http4s-client" % http4sVersion,
    "org.http4s" %% "http4s-server" % http4sVersion,
    // aucune idée le tuto recommande ça aussi 
    "org.typelevel" %% "cats-effect" % catsEffectVersion,

    // Pour Doobie
    "org.tpolecat" %% "doobie-core"     % DoobieVersion,
    "org.tpolecat" %% "doobie-postgres" % DoobieVersion,
    "org.tpolecat" %% "doobie-hikari"   % DoobieVersion,
    "io.estatico"  %% "newtype"         % NewTypeVersion



    // https://http4s.org/v1/docs/json.html
    // ça sera surement utile
    "org.http4s" %% "http4s-circe" % http4sVersion,

    "co.fs2" %% "fs2-core" % fs2Version
)


// Here, `libraryDependencies` is a set of dependencies, and by using `+=`,
// we're adding the scala-parser-combinators dependency to the set of dependencies
// that sbt will go and fetch when it starts up.
// Now, in any Scala file, you can import classes, objects, etc., from
// scala-parser-combinators with a regular import.

// TIP: To find the "dependency" that you need to add to the
// `libraryDependencies` set, which in the above example looks like this:

// "org.scala-lang.modules" %% "scala-parser-combinators" % "2.3.0"

// You can use Scaladex, an index of all known published Scala libraries. There,
// after you find the library you want, you can just copy/paste the dependency
// information that you need into your build file. For example, on the
// scala/scala-parser-combinators Scaladex page,
// https://index.scala-lang.org/scala/scala-parser-combinators, you can copy/paste
// the sbt dependency from the sbt box on the right-hand side of the screen.
