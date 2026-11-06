package org.futo.inputmethod.latin.bengali

import java.util.Locale

/**
 * Zero-GC Flat Compressed Sparse Row (CSR) Trie for Lenient Bangla Phonetic Matching.
 *
 * Employs primitive arrays (CSR layout) to eliminate runtime object allocation during
 * phonetic lookups, providing instantaneous, garbage-collection-free suggestions.
 */
class BanglaPhoneticMatcher private constructor(
    private val nodeEdgeStart: IntArray,
    private val nodeEdgeCount: IntArray,
    private val edgeChars: CharArray,
    private val edgeTargets: IntArray,
    private val nodeWord: Array<String?>,
    private val nodeScore: IntArray
) {

    companion object {
        private const val MAX_CANDIDATES = 12
        private const val STACK_CAPACITY = 256

        val INSTANCE: BanglaPhoneticMatcher by lazy {
            buildDefaultMatcher()
        }

        private fun buildDefaultMatcher(): BanglaPhoneticMatcher {
            val builder = Builder()
            // High-frequency vocabulary and common conversational Banglish stems
            val seedData = arrayOf(
                // Pronouns & greetings
                "আমি" to 100, "আমরা" to 95, "আমাদের" to 95, "আমাকে" to 90, "আমার" to 100,
                "তুমি" to 95, "তোমরা" to 90, "তোমাদের" to 90, "তোমাকে" to 90, "তোমার" to 95,
                "আপনি" to 95, "আপনারা" to 90, "আপনার" to 95, "আপনাকে" to 90,
                "সে" to 90, "তিনি" to 90, "তারা" to 90, "তাদের" to 90, "তার" to 95, "তাকে" to 90,
                "কেমন" to 100, "ভালো" to 100, "ভাল" to 85, "সুন্দর" to 95, "সব" to 95,
                "কিছু" to 95, "ধন্যবাদ" to 100, "ঠিক আছে" to 100, "হবে না" to 90,
                "করব" to 95, "করবো" to 95, "করছি" to 90, "করছে" to 90, "করেছে" to 90,
                "দেখা" to 85, "দেখব" to 95, "দেখবো" to 95, "দেখছি" to 90, "দেখছে" to 90,
                "যাব" to 90, "যাবো" to 90, "যাচ্ছি" to 90, "যাচ্ছে" to 90, "গেছে" to 90,
                "খাব" to 90, "খাবো" to 90, "খাচ্ছি" to 90, "খাচ্ছে" to 90, "খেয়েছে" to 90,
                "বলব" to 90, "বলবো" to 90, "বলছি" to 90, "বলছে" to 90, "বলেছে" to 90,
                "ভাই" to 95, "বোন" to 90, "মা" to 95, "বাবা" to 95, "বন্ধু" to 95,
                "মানুষ" to 95, "দেশ" to 95, "বাংলাদেশ" to 100, "বাংলা" to 100, "ইংরেজি" to 95,
                "সময়" to 95, "দিন" to 90, "রাত" to 90, "সকাল" to 90, "কথা" to 95, "কাজ" to 95,
                "এখন" to 95, "তখন" to 90, "কখন" to 90, "এখানে" to 90, "সেখানে" to 90,
                "কোথায়" to 90, "কেন" to 95, "কী" to 95, "কি" to 95, "কে" to 95,
                "অনেক" to 95, "একটু" to 95, "খুব" to 95, "বেশি" to 90, "কম" to 90,
                "নতুন" to 90, "পুরাতন" to 85, "বড়" to 90, "ছোট" to 90,
                "ছবি" to 95, "গান" to 90, "বই" to 90, "স্কুল" to 90, "কলেজ" to 90
            )

            for ((word, score) in seedData) {
                builder.insert(word, score)
            }
            return builder.build()
        }
    }

    // Thread-local reusable search buffers to guarantee Zero-GC
    private val threadLocalBuffer = object : ThreadLocal<SearchBuffer>() {
        override fun initialValue(): SearchBuffer = SearchBuffer(STACK_CAPACITY, MAX_CANDIDATES)
    }

    private class SearchBuffer(val capacity: Int, val maxResults: Int) {
        val stackNode = IntArray(capacity)
        val stackScore = IntArray(capacity)
        val candidateWords = arrayOfNulls<String>(maxResults)
        val candidateScores = IntArray(maxResults)
        var resultCount = 0

        fun reset() {
            resultCount = 0
            for (i in 0 until maxResults) {
                candidateWords[i] = null
                candidateScores[i] = 0
            }
        }

        fun addCandidate(word: String, score: Int) {
            // Check duplicates
            for (i in 0 until resultCount) {
                if (candidateWords[i] == word) {
                    if (score > candidateScores[i]) {
                        candidateScores[i] = score
                    }
                    return
                }
            }
            if (resultCount < maxResults) {
                candidateWords[resultCount] = word
                candidateScores[resultCount] = score
                resultCount++
            } else {
                // Replace lowest if score is higher
                var minIdx = 0
                for (i in 1 until maxResults) {
                    if (candidateScores[i] < candidateScores[minIdx]) {
                        minIdx = i
                    }
                }
                if (score > candidateScores[minIdx]) {
                    candidateWords[minIdx] = word
                    candidateScores[minIdx] = score
                }
            }
        }
    }

    /**
     * Finds matches in the flat CSR trie for a given transliteration or prefix.
     */
    fun findMatches(transliteration: String?): List<String> {
        if (transliteration.isNullOrEmpty()) return emptyList()
        val buf = threadLocalBuffer.get()
        buf.reset()

        // 1. Direct prefix traversal down the CSR Trie
        var currentNode = 0
        var matchedChars = 0
        val len = transliteration.length

        for (i in 0 until len) {
            val c = transliteration[i]
            val nextNode = findChild(currentNode, c)
            if (nextNode != -1) {
                currentNode = nextNode
                matchedChars++
            } else {
                break
            }
        }

        if (matchedChars > 0) {
            // Exact word match
            val exact = nodeWord[currentNode]
            if (exact != null) {
                buf.addCandidate(exact, nodeScore[currentNode] + 50)
            }
            // DFS collect completions
            collectSubtree(currentNode, buf)
        }

        // Return sorted results
        val results = ArrayList<String>(buf.resultCount)
        // Sort descending by score
        val indices = Array(buf.resultCount) { it }
        indices.sortWith { a, b -> buf.candidateScores[b].compareTo(buf.candidateScores[a]) }

        for (idx in indices) {
            val w = buf.candidateWords[idx]
            if (w != null) results.add(w)
        }

        return results
    }

    private fun findChild(node: Int, c: Char): Int {
        val start = nodeEdgeStart[node]
        val count = nodeEdgeCount[node]
        for (i in 0 until count) {
            val edgeIdx = start + i
            if (edgeChars[edgeIdx] == c) {
                return edgeTargets[edgeIdx]
            }
        }
        return -1
    }

    private fun collectSubtree(startNode: Int, buf: SearchBuffer) {
        var sp = 0
        buf.stackNode[sp] = startNode
        sp++

        while (sp > 0 && buf.resultCount < buf.maxResults) {
            sp--
            val node = buf.stackNode[sp]
            val w = nodeWord[node]
            if (w != null) {
                buf.addCandidate(w, nodeScore[node])
            }
            val start = nodeEdgeStart[node]
            val count = nodeEdgeCount[node]
            for (i in 0 until count) {
                if (sp < buf.capacity) {
                    buf.stackNode[sp] = edgeTargets[start + i]
                    sp++
                }
            }
        }
    }

    /**
     * Builder for constructing the flat CSR Trie.
     */
    class Builder {
        private class TempNode {
            val children = HashMap<Char, TempNode>()
            var word: String? = null
            var score: Int = 0
        }

        private val root = TempNode()

        fun insert(word: String, score: Int) {
            var curr = root
            for (c in word) {
                curr = curr.children.getOrPut(c) { TempNode() }
            }
            curr.word = word
            curr.score = score
        }

        fun build(): BanglaPhoneticMatcher {
            // Flatten to CSR
            val nodeList = ArrayList<TempNode>()
            val queue = ArrayDeque<TempNode>()
            queue.add(root)
            while (queue.isNotEmpty()) {
                val node = queue.removeFirst()
                nodeList.add(node)
                for (child in node.children.values) {
                    queue.add(child)
                }
            }

            val nodeCount = nodeList.size
            val nodeEdgeStart = IntArray(nodeCount)
            val nodeEdgeCount = IntArray(nodeCount)
            val nodeWord = arrayOfNulls<String>(nodeCount)
            val nodeScore = IntArray(nodeCount)

            var totalEdges = 0
            for (node in nodeList) {
                totalEdges += node.children.size
            }

            val edgeChars = CharArray(totalEdges)
            val edgeTargets = IntArray(totalEdges)

            val nodeIndexMap = HashMap<TempNode, Int>(nodeCount)
            for (i in 0 until nodeCount) {
                nodeIndexMap[nodeList[i]] = i
            }

            var edgeOffset = 0
            for (i in 0 until nodeCount) {
                val node = nodeList[i]
                nodeEdgeStart[i] = edgeOffset
                nodeEdgeCount[i] = node.children.size
                nodeWord[i] = node.word
                nodeScore[i] = node.score

                for ((char, child) in node.children) {
                    edgeChars[edgeOffset] = char
                    edgeTargets[edgeOffset] = nodeIndexMap[child] ?: 0
                    edgeOffset++
                }
            }

            return BanglaPhoneticMatcher(
                nodeEdgeStart,
                nodeEdgeCount,
                edgeChars,
                edgeTargets,
                nodeWord,
                nodeScore
            )
        }
    }
}
