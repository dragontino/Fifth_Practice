import ThreadContext.TaskType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.isActive
import java.util.*
import kotlin.math.pow

sealed class Thread(private val name: String, quantum: Int) {

    protected lateinit var scope: CoroutineScope

    protected companion object {
        const val diapason = 1000000000
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
    protected abstract val percent: Double

    private fun isTimeUp() =
        calculateRemainingTime() <= 0

    private fun calculateRemainingTime() =
        quantum - ((System.currentTimeMillis() - startTime) / 1000).toInt()

    protected fun checkAndSave(scope: CoroutineScope, progress: Int, type: TaskType, vararg data: Any) =
        if (isBlocked() || isTimeUp() || !scope.isActive) {
            context.progress = progress

            when (type) {
                TaskType.PRIMES -> {
                    context.primesData.first = data[0] as Long
                    context.primesData.second = data[1] as Int
                }
                TaskType.FERMAT -> {
                    for (i in 0..2)
                        context.fermatData[i] = data[i] as Int
                }
                TaskType.FACTORIALS -> {
                    for (i in 0..2)
                        context.factData[i] = data[i] as Long
                }
            }

            context.remainingTime =
                if (isBlocked())
                    calculateRemainingTime()
                else {
                    status = ThreadStatus.WAITING
                    0
                }

            true
        } else false


    /**
     * функция, запускающая поток
     */
    open suspend fun run(scope: CoroutineScope) {
        this.scope = scope
        status = ThreadStatus.RUNNING
        startTime = System.currentTimeMillis()

        println(this)
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

    val heading: String get() {
        val percentFormatted = String.format(Locale.ENGLISH, "%.1f", this.percent)
        return "Поток: $name: статус: ${status.description.uppercase()}, процент выполнения: $percentFormatted%"
    }
}



class ThreadContext(
    var remainingTime: Int
) {

    enum class TaskType {
        PRIMES,
        FERMAT,
        FACTORIALS
    }

    var progress = 0

    /**
     * Первый элемент - число, которое проверяется на простоту,
     * второй – число, на которое проверяется делимость
     */
    val primesData = Pair(0L, 3)

    /**
     * Первый элемент - текущее число, проверяемое на факториал,
     * второй - факториал, посчитанный на момент остановки потока,
     * третий - индекс факториала
     */
    val factData = LongArray(3) { 1L }

    /**
     * элементы - числа a, b и c, для которых проверяется условие Ферма
     */
    val fermatData = IntArray(3)
}



enum class ThreadStatus(val description: String) {
    RUNNING("Выполняется"),
    WAITING("Ожидает"),
    BLOCKED("Заблокирован"),
    COMPLETED("Выполнен")
}



class PrimesThread(name: String, quantum: Int) : Thread(name, quantum) {

    private companion object {
        const val diapason = 50000000
    }

    private var countPrimes = context.progress
    private var currentI = 0L

    override suspend fun run(scope: CoroutineScope) {
        super.run(scope)
        val start = context.primesData.first

        for (i in start..diapason) {
            currentI = i

            if (i.isPrime())
                countPrimes++

            if (status != ThreadStatus.RUNNING)
                return
        }

        context.progress = countPrimes
        status = ThreadStatus.COMPLETED
    }

    override val resultString = "Количество простых чисел до $diapason"
    override val percent: Double
    get() = currentI.toDouble() / diapason * 100

    private fun Long.isPrime(): Boolean {
        if (this % 2 == 0L)
            return false

        val start = if (context.primesData.first == this)
            context.primesData.second
        else 3

        for (i in start..sqrt(this) step 2) {

            if (checkAndSave(scope, countPrimes, TaskType.PRIMES, this, i))
                return false

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
    private var currentValue: Long = 0

    override suspend fun run(scope: CoroutineScope) {
        super.run(scope)

        for (value in context.factData[0]..diapason) {
            currentValue = value

            if (value.isFactorial()) {
                if (status != ThreadStatus.RUNNING)
                    return
                countFact++
            }
        }

        context.progress = countFact
        status = ThreadStatus.COMPLETED
    }

    private fun Long.isFactorial(): Boolean {
        var fact = context.factData[1]
        var i = context.factData[2]
        while (fact < this) {
            fact *= i
            i++

            if (checkAndSave(scope, countFact, TaskType.FACTORIALS, this, fact, i))
                return false
        }
        return fact == this
    }

    override val resultString = "Количество факториалов от 1 до $diapason"
    override val percent: Double
        get() = currentValue.toDouble() * 100 / diapason
}





class FermatThread(name: String, quantum: Int) : Thread(name, quantum) {

    private companion object {
        const val diapason = 10000
    }

    private var countFermat = context.progress
    private var currentProgress = 0

    override suspend fun run(scope: CoroutineScope) {
        super.run(scope)

        for (a in context.fermatData[0]..diapason)
            for (b in context.fermatData[1]..diapason)
                for (c in context.fermatData[2]..(diapason)) {

                    currentProgress = a * (getDiapasonLength(1) * getDiapasonLength(2)) +
                            b * getDiapasonLength(2) + c

                    if (checkAndSave(scope, progress = countFermat, type = TaskType.FERMAT, a, b, c))
                        return

                    if ((a pow 3) + (b pow 3) == (c pow 3)) {
                        countFermat++
                    }
                }

        context.progress = countFermat
        status = ThreadStatus.COMPLETED
    }

    private fun getDiapasonLength(i: Int) =
        diapason - context.fermatData[i]

    private infix fun Int.pow(n: Int) =
        toDouble().pow(n).toInt()

    override val resultString = "Количество опровержений теоремы Ферма в диапазоне от 0 до $diapason"
    override val percent: Double
        get() = currentProgress.toDouble() / (diapason pow 3) * 100

}