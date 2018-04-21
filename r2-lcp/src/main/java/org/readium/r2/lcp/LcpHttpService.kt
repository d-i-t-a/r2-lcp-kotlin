package org.readium.r2.lcp

import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Environment
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.result.Result
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.deferred
import nl.komponents.kovenant.task
import nl.komponents.kovenant.then
import org.json.JSONObject
import org.readium.r2.lcp.Model.Documents.LicenseDocument
import org.readium.r2.lcp.Model.Documents.StatusDocument
import java.io.File
import java.nio.charset.Charset
import java.util.*

class LcpHttpService {

    fun statusDocument(url: String): Promise<StatusDocument, Exception> {
        return Fuel.get(url,null).promise() then {
            val (request, response, result) = it
            StatusDocument(result)
        }
    }
    fun fetchUpdatedLicense(url: String): Promise<LicenseDocument, Exception> {
        return Fuel.get(url,null).promise() then {
            val (request, response, result) = it
            LicenseDocument(result)
        }
    }

    fun publicationUrl(url: String, parameters: List<Pair<String, Any?>>? = null): Promise<String, Exception> {
        val uuid = UUID.randomUUID().toString()
        val EPUB_FILE_NAME = uuid
        val rootDir: String = android.os.Environment.getExternalStorageDirectory().path + "/r2test/"
        return Fuel.download(url).destination { response, destination ->
            Log.i("FUEL destination ", rootDir +  EPUB_FILE_NAME)
            File(rootDir, EPUB_FILE_NAME)
        }.promise() then {
            val (request, response, result) = it
            Log.i("FUEL destination ", rootDir +  EPUB_FILE_NAME)
            Log.i("FUEL then ", response.url.toString())
            rootDir +  EPUB_FILE_NAME
        }
    }

    fun certificateRevocationList(url: String): Promise<String, Exception> {
        return Fuel.get(url,null).promise() then {
            val (request, response, result) = it
            "-----BEGIN X509 CRL-----${Base64.getEncoder().encodeToString(result)}-----END X509 CRL-----";
        }
    }
    
    fun register(registerUrl: String, params: List<Pair<String, Any?>>): Promise<String?, Exception> {
        return Fuel.post(registerUrl.toString(), params).promise() then {
            val (request, response, result) = it
            var status:String? = null
                if (response.statusCode.equals(200)) {
                val jsonObject = JSONObject(String(result, Charset.forName(response.contentTypeEncoding)))
                status = jsonObject["status"] as String
            }
            status
        }
    }

    fun renewLicense(url: String): Promise<String?, Exception> {
        return task { null }
    }

    fun returnLicense(url: String): Promise<String?, Exception> {
        return task { null }
    }

}


fun Request.promise(): Promise<Triple<Request, Response, ByteArray>, Exception> {
    val deferred = deferred<Triple<Request, Response, ByteArray>, Exception>()
    task { response() } success {
        val (request, response, result) = it
        when(result) {
            is Result.Success -> deferred.resolve(Triple(request, response, result.value))
            is Result.Failure -> deferred.reject(result.error)
        }
    } fail {
        deferred.reject(it)
    }
    return deferred.promise
}

val Response.contentTypeEncoding: String
    get() = contentTypeEncoding()

fun Response.contentTypeEncoding(default: String = "utf-8"): String {
    val contentType: String = httpResponseHeaders["Content-Type"]?.first() ?: return default
    return contentType.substringAfterLast("charset=", default).substringAfter(' ', default)
}




