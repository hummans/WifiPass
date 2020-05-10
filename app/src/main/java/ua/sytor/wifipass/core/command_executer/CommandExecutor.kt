package ua.sytor.wifipass.core.command_executer

import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.concurrent.Executors

class CommandExecutor : CommandExecutorContract.CommandExecutor {

	val executorsPool = Executors.newFixedThreadPool(2)
		.asCoroutineDispatcher()

	@Throws(Exception::class)
	override suspend fun execCommand(command: String): String {
		return execCommand(command, 2000)
	}

	@Throws(Exception::class)
	override suspend fun execCommand(command: String, timeout: Long): String {
		val process = Runtime.getRuntime().exec(command)

		val output = withContext(executorsPool) { getStreamOutput(process.inputStream) }
		val error = withContext(executorsPool) { getStreamOutput(process.errorStream) }

		process.waitFor()

		return if (error.isEmpty())
			output
		else
			throw Exception(error)

	}

	@Throws(IOException::class)
	private fun getStreamOutput(inputStream: InputStream): String {

		val bufferedReader = BufferedReader(
			InputStreamReader(inputStream))

		val log = StringBuilder()
		var line = bufferedReader.readLine()
		while (line != null) {
			log.append(line + "\n")
			line = bufferedReader.readLine()
		}

		return log.toString()
	}

}
