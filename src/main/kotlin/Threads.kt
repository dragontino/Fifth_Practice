import kotlinx.coroutines.*

@DelicateCoroutinesApi
sealed class Thread(val threadName: String, quantum: Int) : java.lang.Thread() {
    var quantum = quantum
        set(value) {
            context.remainingTime = value
            field = value
        }

    protected var status = ThreadStatus.WAITING
    protected val context = ThreadContext(status, quantum)
    private var startTime = 0L

    protected abstract fun getResultString(): String

    protected fun calculateTime() =
        ((System.currentTimeMillis() - startTime) / 1000).toInt()

    override fun run() {
        super.run()
        status = ThreadStatus.RUNNING
        startTime = System.currentTimeMillis()

        GlobalScope.launch {
            if (status != ThreadStatus.RUNNING)
                cancel()

            val time = calculateTime()

            if (time >= quantum) {
                status = ThreadStatus.WAITING
                context.status = status
                cancel()
            }

            context.remainingTime = quantum - time
            delay(1000L)
        }
    }

    fun block() {
        status = ThreadStatus.BLOCKED
    }

    fun unlock() {
        status = ThreadStatus.WAITING
    }

    fun isBlocked() =
        status == ThreadStatus.BLOCKED

    fun isCompleted() =
        status == ThreadStatus.COMPLETED

    override fun toString() =
        "Поток $threadName: статус - ${status.description}, ${ 
            if (isCompleted()) "${getResultString()} – ${context.progress}" 
            else "квант – $quantum"
        }"
}



class ThreadContext(
    var status: ThreadStatus,
    var remainingTime: Int
) {
    var progress = 0
    val data: Pair<Long, Long> = Pair(0, 0)
}



enum class ThreadStatus(val description: String) {
    RUNNING("Выполняется"),
    WAITING("Ожидает"),
    BLOCKED("Заблокирован"),
    COMPLETED("Выполнен")
}



@DelicateCoroutinesApi
class PrimesThread(name: String, quantum: Int) : Thread(name, quantum) {
    private var countPrimes = context.progress

    private companion object {
        const val diapason = 10000000
    }

    override fun run() {
        super.run()
        val start = context.data[0]

        for (i in start..diapason) {

            if (checkAndSave(i, 0)) return

            if (isPrime(i.toInt())) {
                if (context.data[1] > 0) {
                    context.data[0] = i
                    return
                }
                countPrimes++
            }
        }

        context.progress = countPrimes
        status = ThreadStatus.COMPLETED
    }

    override fun getResultString() =
        "Количество простых чисел до $diapason"

    private fun isPrime(n: Int): Boolean {
        if (n % 2 == 0)
            return false

        for (i in 3..sqrt(n) step 2) {

            if (checkAndSave(0, i))
                return false

            if (n % i == 0)
                return false
        }
        return true
    }

    private fun checkAndSave(i: Long, j: Int): Boolean {

        if (status == ThreadStatus.BLOCKED) {
            val time = calculateTime()
            context.status = status
            context.remainingTime = quantum - time
        }

        return if (status == ThreadStatus.WAITING) {
            context.progress = countPrimes
            context.data[0] = i
            context.data[1] = j.toLong()
            true
        } else false
    }

    private fun sqrt(n: Number) =
        kotlin.math.sqrt(n.toDouble()).toInt()
}



@DelicateCoroutinesApi
class FibonacciThread(name: String, quantum: Int) : Thread(name, quantum) {
    override fun run() {
        super.run()
    }

    override fun getResultString(): String {
        TODO("Not yet implemented")
    }
}



@DelicateCoroutinesApi
class FermatThread(name: String, quantum: Int) : Thread(name, quantum) {

    override fun run() {
        super.run()
    }

    override fun getResultString(): String {
        TODO("Not yet implemented")
    }

}