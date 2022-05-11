sealed class Thread(val name: String, quantum: Int) {

    protected companion object {
        const val diapason = 100000000000
    }

    var quantum = quantum
        set(value) {
            context.remainingTime = value
            field = value
        }

    protected var status = ThreadStatus.WAITING

    /** контекст потока */
    protected val context = ThreadContext(quantum)
    /**
     * начальное время (в миллисекундах)
     * */
    private var startTime = 0L


    protected abstract val resultString: String

    protected fun isTimeUp() =
        calculateRemainingTime() <= 0

    protected fun calculateRemainingTime() =
        quantum - ((System.currentTimeMillis() - startTime) / 1000).toInt()


    /**
     * функция, запускающая поток
     */
    open suspend fun run() {
        status = ThreadStatus.RUNNING
        startTime = System.currentTimeMillis()
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
        "$heading, ${ 
            if (isCompleted()) "$resultString: ${context.progress}" 
            else "квант = $quantum"
        }"


    val heading: String get() =
        "Поток: $name: статус: ${status.description}"
}



class ThreadContext(
    var remainingTime: Int
) {
    var progress = 0
    val primesData = Pair(0L, 3)
    val factData = 0
}



enum class ThreadStatus(val description: String) {
    RUNNING("Выполняется"),
    WAITING("Ожидает"),
    BLOCKED("Заблокирован"),
    COMPLETED("Выполнен")
}



class PrimesThread(name: String, quantum: Int) : Thread(name, quantum) {
    private var countPrimes = context.progress

    override suspend fun run() {
        super.run()
        val start = context.primesData.first

        for (i in start..diapason) {

            if (i.isPrime())
                countPrimes++

            if (status != ThreadStatus.RUNNING)
                return
        }

        context.progress = countPrimes
        status = ThreadStatus.COMPLETED
    }

    override val resultString = "Количество простых чисел до $diapason"

    private fun Long.isPrime(): Boolean {
        if (this % 2 == 0L)
            return false

        val start = if (context.primesData.first == this)
            context.primesData.second
        else 3

        for (i in start..sqrt(this) step 2) {

            if (isBlocked() || isTimeUp()) {
                context.progress = countPrimes
                context.primesData.first = this
                context.primesData.second = i

                context.remainingTime =
                    if (isBlocked())
                        calculateRemainingTime()
                    else {
                        status = ThreadStatus.WAITING
                        0
                    }

                return false
            }

            if (this % i == 0L)
                return false
        }
        return true
    }


    private fun sqrt(n: Number) =
        kotlin.math.sqrt(n.toDouble()).toInt()
}






class FactorialsThread(name: String, quantum: Int) : Thread(name, quantum) {

    private var countFact = context.progress

    override suspend fun run() {
        super.run()
        val start = context.factData
    }

    override val resultString = "Количество факториалов от 0 до $diapason"
}





class FermatThread(name: String, quantum: Int) : Thread(name, quantum) {

    override suspend fun run() {
        super.run()

    }

    override val resultString = "Количество опровержений теоремы Ферма в диапазоне от 0 до $diapason"

}