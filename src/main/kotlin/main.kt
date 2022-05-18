import kotlinx.coroutines.*
import java.util.*


private const val countThreads = 3
private lateinit var threadQueue: Array<Thread>
private val menu = Menu()


@DelicateCoroutinesApi
fun main() {
    val input = Scanner(System.`in`)

    threadQueue = Array(countThreads) {
        print("Введите квант для ${it + 1} потока: ")
        val quantum = input.nextInt()

        when (it) {
            0 -> PrimesThread("Поиск простых чисел", quantum)
            1 -> FactorialsThread("Вычисление факториалов", quantum)
            else -> FermatThread("Поиск опровержения теоремы Ферма", quantum)
        }
    }


    /**
     * корутина для запуска "потоков"
     */
    val job = CoroutineScope(Dispatchers.Default).launch {
        while (true) {
            if (!this.isActive)
                return@launch

            val notCompletedThreads = threadQueue.filter {
                !it.isCompleted() && !it.isBlocked()
            }
            if (notCompletedThreads.isEmpty())
                return@launch

            val currentThread = notCompletedThreads.maxByOrNull { it.quantum }

            println(currentThread)
            currentThread?.run(this)

            if (currentThread?.isBlocked() == false)
                println(currentThread)
        }
    }


    runBlocking {
        while (true) {
            if (threadQueue.find { !it.isCompleted() } == null)
                break

//            delay(100)

            menu.printThreadsInfo(
                "Для управления потоками введите его номер из списка ниже:")

            var cmd = input.nextInt()

            if (cmd !in 1..menu.countCommands) {
                println("Неправильная команда!")
                continue
            }

            if (cmd == countThreads + 1)
                continue
            if (cmd == countThreads + 2) {
                println("Завершение работы...")
                break
            }

            val currentThread = threadQueue[cmd - 1]

            launch {
                while (true) {
                    if (currentThread.isCompleted()) {
                        println("\nЭтот поток уже выполнен, выберите другой!")
                        continue
                    }
                }
            }

            menu.printThreadMenu(currentThread)
            cmd = input.nextInt()
            if (cmd !in 1..countThreads) {
                println("Неправильная команда!")
                continue
            }

            when (cmd) {
                1 -> {
                    println("Текущий квант: ${currentThread.quantum}")
                    println("Введите квант: ")
                    val q = input.nextInt()
                    if (q > 0)
                        currentThread.quantum = q
                }
                2 -> {
                    if (currentThread.isBlocked()) {
                        currentThread.unlock()
                        println("Поток разблокирован!")
                    } else {
                        currentThread.block()
                        println("Поток заблокирован!")
                    }
                }
            }
            continue
        }
        job.cancelAndJoin()
    }

    menu.printThreadsInfo()
}


private class Menu {

    val countCommands = countThreads + 2

    fun printThreadsInfo(menuHeader: String = "") {
        if (menuHeader.isNotEmpty())
            println("\n$menuHeader")
        threadQueue.forEachIndexed { index, thread ->
            println("${index + 1}. $thread")
        }
        println("${countThreads + 1}. Обновить данные")
        println("${countThreads + 2}. Завершить работу")
        print(">> ")
    }

    fun printThreadMenu(thread: Thread) {
        println("\n${thread.heading}")
        println("Выберите действие и введите его номер ниже")
        println("1. Изменить квант")
        println(
            if (thread.isBlocked()) "2. Разблокировать"
            else "2. Заблокировать"
        )
        println("3. Вернуться назад")
        println(">> ")
    }
}