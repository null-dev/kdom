package xyz.nulldev.kdom.api.routing

import decodeURIComponent
import org.w3c.dom.url.URL
import kotlin.browser.window

class RouteContext(
        val path: List<String>,
        val queryParams: Map<String, List<String>>,
        val prevPath: List<String>?,
        val prevQueryParams: Map<String, List<String>>?
) {
    var finished = false
        private set

    fun finish() {
        finished = true
    }

    var urlVars: Map<String, String> = emptyMap()
        get() {
            //Rebuild cached map if vars have changed
            if(urlVarStackDirty)
                field = urlVarStack.toMap()

            return field
        }
        private set
    private var urlVarStackDirty = false
    private val urlVarStack = mutableListOf<Pair<String, String>>()

    private val mutableTags = mutableMapOf<String, Any>()
    val tags = mutableTags.toMap()

    val relativePath
        get() = path.drop(relativePathStartIndex)

    val currentPath
        get() = path.take(relativePathStartIndex)

    private var relativePathStartIndex = 0

    fun popRelativePath() {
        if(relativePathStartIndex <= path.lastIndex)
            relativePathStartIndex++
        else
            throw IllegalStateException("Already at beginning of path!")
    }

    fun pushRelativePath() {
        if(relativePathStartIndex > 0)
            relativePathStartIndex--
        else
            throw IllegalStateException("Already at end of path!")
    }

    fun addTag(name: String, value: Any) {
        mutableTags.put(name, value)
    }

    fun <T : Any> getTag(name: String): T? {
        return mutableTags[name]?.let { it as T }
    }

    fun <T : Any> removeTag(name: String): T? {
        return mutableTags.remove(name)?.let { it as T }
    }

    fun pushUrlVar(name: String, value: String) {
        urlVarStack.add(name to value)
        urlVarStackDirty = true
    }

    fun popUrlVar(): Pair<String, String> {
        if(urlVarStack.isEmpty())
            throw IllegalStateException("Url var stack is empty!")

        urlVarStackDirty = true
        return urlVarStack.removeAt(urlVarStack.lastIndex)
    }

    companion object {
        fun from(path: String, prevPath: String?): RouteContext {
            val parsedPath = parsePath(path)
            val parsedPrevPath = prevPath?.let { parsePath(it) }

            return RouteContext(parsedPath.first, parsedPath.second, parsedPrevPath?.first, parsedPrevPath?.second)
        }

        private fun parsePath(path: String): Pair<List<String>, Map<String, List<String>>> {
            val parsed = URL(path, window.location.let {
                it.protocol + "//" + it.host
            })
            val splitPath = parsed.pathname.split("/").map {
                decodeURIComponent(it)
            }.filterNot { it.isBlank() }.toMutableList()

            //Root is actually a page with a blank url
            if(splitPath.isEmpty())
                splitPath += ""

            //Manually process query string
            val splitQuery: Map<String, List<String>> = if(parsed.search.isNotEmpty()) {
                val searchSeg = parsed.search.substring(1)
                val out = mutableMapOf<String, MutableList<String>>()
                searchSeg.split("&").filterNot { it.isBlank() }.map {
                    val split = it.split("=")
                    Pair(split.first(), split.last())
                }.forEach {
                    val target = out.getOrPut(it.first, { mutableListOf() })
                    target += it.second
                }
                out
            } else emptyMap()
            return Pair(splitPath, splitQuery)
        }
    }
}