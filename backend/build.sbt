scalaVersion := "3.3.1"

name := "hello-world"

version := "0.1"

// FS2 parce que c'est beaucoup plus expliqué au niveau de la doc cf (notre exposé sur les streams)
val fs2Version = "3.9.2"

// https://http4s.org/
// Askip c'est good avec fs2 cf premiere page
val http4sVersion = "0.23.26"

// ??????????
val catsEffectVersion = "3.5.2"

// Le tuto disat RC1 mais si c'est pas le RC4 ça crash de mon coté
val DoobieVersion = "1.0.0-RC4"
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
    "org.tpolecat" %% "doobie-core" % DoobieVersion,
    "org.tpolecat" %% "doobie-hikari" % DoobieVersion,
    "ru.yandex.clickhouse" % "clickhouse-jdbc" % "0.3.2",

   

    // https://http4s.org/v1/docs/json.html
    // ça sera surement utile
    "org.http4s" %% "http4s-circe" % http4sVersion,

    "co.fs2" %% "fs2-core" % fs2Version,
    "com.github.fd4s" %% "fs2-kafka" % "3.7.0",

    // c'est pour faire des logins avec les JWT 
    // comme on utilise un truc similaire en python pour le pfe ça devrait aller
    // https://jwt-scala.github.io/jwt-scala/jwt-circe.html
    // Pendant longtemps j'ai eu un probleme avec l'IDE qui met des erreur la dessus,
    // mais quand on test ça marche alors ça m'a l'air ok ?
    "com.github.jwt-scala" %% "jwt-core" % "10.0.4",
    "com.github.jwt-scala" %% "jwt-circe" % "10.0.4",
    "org.scala-lang" %% "toolkit" % "0.7.0",
    // ce truc ne marche que pour scala 2 DOnc NON
    // "com.github.t3hnar" %% "scala-bcrypt" % "4.4.0",
    // ce truc marche pour scala 3
    "at.favre.lib" % "bcrypt" % "0.10.2",
)