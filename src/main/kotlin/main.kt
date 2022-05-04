import kotlinx.coroutines.*
import java.util.*


@DelicateCoroutinesApi
fun main() {
    val input = Scanner(System.`in`)

    threadQueue = Array(3) {
        print("Введите квант для ${it + 1} потока: ")
        val quantum = input.nextInt()
        PrimesThread("Поиск простых чисел", quantum)

//        when (it) {
//            0 -> PrimesThread("Поиск простых чисел", quantum)
//            1 -> FibonacciThread("Вычисление последовательности Фибоначчи", quantum)
//            else -> FermatThread("Поиск опровержения теоремы Ферма", quantum)
//        }
    }

    val currentThread = threadQueue.maxByOrNull { it.quantum }
    currentThread?.start()

    GlobalScope.launch(SupervisorJob() + Dispatchers.IO) {

        printMenu()

        var thread: Thread
        var cmd: Int

        do {
            do {
                cmd = input.nextInt()
                thread = threadQueue[cmd - 1]

                thread.checkIfNotCompleted()
            } while (thread.isCompleted())

            println("\nПоток: ${thread.threadName}")
            println("Выберите действие и введите его номер ниже")
            println("1. Изменить квант")
            println(
                if (thread.isBlocked()) "2. Разблокировать"
                else "2. Заблокировать"
            )
            println("3. Вернуться назад")

            cmd = input.nextInt()
            thread.checkIfNotCompleted()

            do {
                when (cmd) {
                    1 -> {
                        println("Введите квант: ")
                        val q = input.nextInt()
                        if (q > 0)
                            thread.quantum = q
                        break
                    }
                    2 -> {
                        if (thread.isBlocked())
                            thread.unlock()
                        else
                            thread.block()
                        break
                    }
                    else -> {
                        println("Такого пункта нет! Пожалуйста, выберите другой")
                        cmd = input.nextInt()
                    }
                }
            } while (true)

        } while (thread.isCompleted())
    }

    currentThread?.join()
    println()
    threadQueue.forEachIndexed { index, thread ->
        println("${index + 1}. $thread")
    }
}

@DelicateCoroutinesApi
private fun printMenu() {
    println("\nДля управления потоками введите его номер в списке ниже:")
    threadQueue.forEachIndexed { index, thread ->
        println("${index + 1}. $thread")
    }
}

@DelicateCoroutinesApi
fun Thread.checkIfNotCompleted() {
    if (isCompleted()) {
        println("Поток выполнен, выберите другой!")
        printMenu()
    }
}

@DelicateCoroutinesApi
private lateinit var threadQueue: Array<Thread>