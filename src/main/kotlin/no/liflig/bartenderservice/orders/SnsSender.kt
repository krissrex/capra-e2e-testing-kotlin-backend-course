package no.liflig.bartenderservice.orders

import software.amazon.awssdk.services.sns.SnsClient

interface SnsSender {
  fun send(message: String)
}

class AwsSnsSender(
    private val topicArn: String,
    private val snsClient: SnsClient,
) : SnsSender {
  override fun send(message: String) {
    snsClient.publish { req ->
      req.topicArn(topicArn)
      req.message(message)
    }
  }
}
