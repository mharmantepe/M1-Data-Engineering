package client

import client.Citizen
import java.util.{Date, Properties}
import scala.util.Random
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import scala.io.Source
import play.api.libs.json.Json
import play.api.libs.json._

object Producer extends App{
  val props = new Properties()
  props.put("bootstrap.servers", "localhost:9092")
  props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  props.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer")

  val topic = "reports"

  val producer = new KafkaProducer[String, Array[Byte]](props)

  val words = List("hello", "depressed", "good", "happy", "tired", "sad", "exhausted", "joyous", "dancing", "crying")

  def readCSV(filename: String): List[String] = {
    val source = Source.fromFile(filename)
    val lines = source.getLines.toList
    source.close()
    lines
    // Split each line into fields and convert to a List[List[String]]
    //lines.map(line => line.split(",").map(_.trim).toList)
  }

  def generateReport(): JsValue = {
    val droneId = 1+ Random.nextInt(5)
    val longitude = Random.nextDouble() * 360 - 180
    val latitude = Random.nextDouble() * 180 - 90
    val timestamp = new Date()

    // Generate a random count between 1 and 10
    val randomNbCitizen = Random.nextInt(10) + 1
    val citizens = List.fill(randomNbCitizen)(client.Citizen.generateCitizen())

    // Choose a number of words to include in the report between 2 and 10
    val randomNbWords = 2 + Random.nextInt(9)
    // Generate the list of words by selecting them at random
    val rand_words = List.fill(randomNbWords)(words(Random.nextInt(words.length)))

    Json.toJson(Map("drone_id" -> droneId.toString, "longitude" -> longitude.toString, "latitude" -> latitude.toString,
      "timestamp" -> timestamp.toString, "citizens" -> citizens.toString, "words" -> rand_words.mkString(",")))
  }

  while (true) {
    val data = generateReport()
    val report = new ProducerRecord[String, Array[Byte]](topic, Json.toBytes(data))
    producer.send(report)
    Thread.sleep(60000) // sleep for 1 minute
  }
  //val namesList = readCSV("")
  producer.close()
}
