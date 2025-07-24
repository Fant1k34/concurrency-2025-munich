package day1

import java.util.concurrent.atomic.AtomicReference

class MSQueue<E> : Queue<E> {
    private val head: AtomicReference<Node<E>>
    private val tail: AtomicReference<Node<E>>

    init {
        val dummy = Node<E>(null)
        head = AtomicReference(dummy)
        tail = AtomicReference(dummy)
    }

    override fun enqueue(element: E) {
        val newNode = Node(element)

        while (true) {
            // Запоминаем хвостовой элемент
            val currentTail = tail.get()

            // Хвост -- последний элемент, у которого ссылка на следующий равна null
            // Если у currentTail и правда нет ссылки (к текущему моменту tail мог уже поменяться),
            // то тогда currentTail остался актуальным,
            // поэтому мы добавляем к нему атомарно ссылку на созданную ноду
            if (currentTail.next.compareAndSet(null, newNode)) {
                // Теперь заменяем значение Node внутри tail
                tail.compareAndSet(currentTail, newNode)
                return
            } else {
                // Если у tail есть ссылка на элемент, то тогда другой поток уже добавил ссылку на новую ноду
                // Но не успел обновить хвост
                // Поможем ему -- сделаем это за него и начнем сначала
                tail.compareAndSet(currentTail, currentTail.next.get())
            }
        }
    }

    override fun dequeue(): E? {
        while (true) {
            val currentHead = head.get()
            val nextOfCurrentHead = currentHead.next.get()

            // Если ссылка на следующий элемент отсутствует, то текущий элемент dummy, и нечего удалять
            // Dummy -- обязятельная и единственная нода-пустышка, которая имеет значение null
            if (nextOfCurrentHead == null) return null

            // Если head не поменялся, то тогда заменяем его на следующий его элемент
            if (head.compareAndSet(currentHead, nextOfCurrentHead)) {
                // Запоминаем значение в следующем элементе
                val result = nextOfCurrentHead.element

                // Превращаем следующий элемент в пустышку
                nextOfCurrentHead.element = null
                return result
            }
            // Если head изменился, пробуем заного
        }
    }

    // FOR TEST PURPOSE, DO NOT CHANGE IT.
    override fun validate() {
        check(tail.get().next.get() == null) {
            "`tail.next` must be `null`"
        }
        check(head.get().element == null) {
            "`head.element` must be `null`"
        }
    }

    private class Node<E>(
        var element: E?
    ) {
        val next = AtomicReference<Node<E>?>(null)
    }
}
