package com.learnly.seed;

import com.learnly.entity.*;
import com.learnly.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository              users;
    private final CourseRepository            courses;
    private final LessonRepository            lessons;
    private final InterviewQuestionRepository questions;
    private final PasswordEncoder             encoder;

    public DataSeeder(UserRepository users, CourseRepository courses,
                      LessonRepository lessons, InterviewQuestionRepository questions,
                      PasswordEncoder encoder) {
        this.users = users;
        this.courses = courses;
        this.lessons = lessons;
        this.questions = questions;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedAdmin();
        seedCoursesAndLessons();
        seedInterviewQuestions();
    }

    private void seedAdmin() {
        if (users.existsByEmail("[email protected]")) return;
        users.save(User.builder()
                .name("Admin")
                .email("[email protected]")
                .phone("+10000000000")
                .passwordHash(encoder.encode("Admin123!"))
                .role(User.Role.ADMIN)
                .plan(User.Plan.PRO)
                .language("en")
                .emailVerified(true)
                .build());

        // Demo PRO learner so the AI assistant works out of the box
        if (!users.existsByEmail("[email protected]")) {
            users.save(User.builder()
                    .name("Demo")
                    .email("[email protected]")
                    .phone("+10000000001")
                    .passwordHash(encoder.encode("Demo1234!"))
                    .role(User.Role.USER)
                    .plan(User.Plan.PRO)
                    .language("en")
                    .emailVerified(true)
                    .build());
        }
    }

    private void seedCoursesAndLessons() {
        if (courses.count() > 0) return;

        Course py = courses.save(Course.builder()
                .slug("python-basics")
                .title("Python Basics").titleRu("Основы Python")
                .description("Start your programming journey with Python — variables, control flow, and your first real programs.")
                .descriptionRu("Начните свой путь в программировании с Python — переменные, управляющие конструкции и первые программы.")
                .language("Python").level(Course.Level.BEGINNER)
                .coverUrl("https://images.unsplash.com/photo-1526379095098-d400fd0bf935?w=1200")
                .build());

        Course java = courses.save(Course.builder()
                .slug("java-basics")
                .title("Java Foundations").titleRu("Основы Java")
                .description("A practical introduction to Java — syntax, types, and writing your first class.")
                .descriptionRu("Практическое введение в Java — синтаксис, типы и первый класс.")
                .language("Java").level(Course.Level.BEGINNER)
                .coverUrl("https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=1200")
                .build());

        Course go = courses.save(Course.builder()
                .slug("go-basics")
                .title("Go Foundations").titleRu("Основы Go")
                .description("Learn Go from the ground up — packages, functions, and idiomatic style.")
                .descriptionRu("Изучайте Go с нуля — пакеты, функции и идиоматический стиль.")
                .language("Go").level(Course.Level.BEGINNER)
                .coverUrl("https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=1200")
                .build());

        // Python: 2 lessons
        lessons.save(Lesson.builder()
                .course(py).position(1)
                .title("Hello, Python").titleRu("Привет, Python")
                .videoUrl("https://www.youtube.com/embed/kqtD5dpn9C8")
                .description("In Python, printing to the console is done with the built-in print() function. Strings can be wrapped in single or double quotes. This is the universal \"first program\" in any language.")
                .descriptionRu("В Python вывод в консоль выполняется встроенной функцией print(). Строки можно заключать в одинарные или двойные кавычки. Это универсальная «первая программа» в любом языке.")
                .task("Write a program that prints exactly: Hello, World!")
                .taskRu("Напишите программу, которая выводит ровно: Hello, World!")
                .expectedSolution("print(\"Hello, World!\")")
                .build());

        lessons.save(Lesson.builder()
                .course(py).position(2)
                .title("Variables and Types").titleRu("Переменные и типы")
                .videoUrl("https://www.youtube.com/embed/cQT33yu9pY8")
                .description("Variables in Python are dynamically typed. Common built-in types include int, float, str, and bool. You can inspect a value's type with the type() function.")
                .descriptionRu("Переменные в Python имеют динамическую типизацию. Среди встроенных типов: int, float, str, bool. Тип значения можно проверить функцией type().")
                .task("Create a variable name with your name and age with your age (an integer), then print: \"I am {name} and I am {age} years old\".")
                .taskRu("Создайте переменную name с вашим именем и age с вашим возрастом (целое), затем выведите: \"I am {name} and I am {age} years old\".")
                .expectedSolution("name = \"Ada\"\nage = 30\nprint(f\"I am {name} and I am {age} years old\")")
                .build());

        // Java: 1 lesson
        lessons.save(Lesson.builder()
                .course(java).position(1)
                .title("Your First Java Class").titleRu("Первый класс на Java")
                .videoUrl("https://www.youtube.com/embed/eIrMbAQSU34")
                .description("Every Java program starts inside a class. The entry point is the public static void main(String[] args) method. System.out.println prints a line to the console.")
                .descriptionRu("Каждая программа на Java начинается внутри класса. Точка входа — метод public static void main(String[] args). System.out.println печатает строку в консоль.")
                .task("Write a class Main with a main method that prints exactly: Hello, Java!")
                .taskRu("Напишите класс Main с методом main, который выводит ровно: Hello, Java!")
                .expectedSolution("public class Main {\n  public static void main(String[] args) {\n    System.out.println(\"Hello, Java!\");\n  }\n}")
                .build());

        // Go: 1 lesson
        lessons.save(Lesson.builder()
                .course(go).position(1)
                .title("Hello from Go").titleRu("Привет от Go")
                .videoUrl("https://www.youtube.com/embed/YS4e4q9oBaU")
                .description("A Go program belongs to a package. Executable programs use package main and a func main() entry point. fmt.Println writes a line to standard output.")
                .descriptionRu("Программа на Go принадлежит пакету. Исполняемые программы используют package main и точку входа func main(). fmt.Println печатает строку в стандартный вывод.")
                .task("Write a Go program that prints exactly: Hello, Go!")
                .taskRu("Напишите программу на Go, которая выводит ровно: Hello, Go!")
                .expectedSolution("package main\nimport \"fmt\"\nfunc main() { fmt.Println(\"Hello, Go!\") }")
                .build());
    }

    private void seedInterviewQuestions() {
        if (questions.count() > 0) return;
        questions.save(InterviewQuestion.builder()
                .title("Reverse a String").titleRu("Перевернуть строку")
                .prompt("Write a function reverse(s) that returns the input string s reversed.")
                .promptRu("Напишите функцию reverse(s), которая возвращает входную строку s в обратном порядке.")
                .language("Python").difficulty(InterviewQuestion.Difficulty.EASY).build());
        questions.save(InterviewQuestion.builder()
                .title("FizzBuzz").titleRu("FizzBuzz")
                .prompt("Print numbers 1..15. For multiples of 3 print Fizz, of 5 print Buzz, of both print FizzBuzz.")
                .promptRu("Выведите числа 1..15. Для кратных 3 — Fizz, кратных 5 — Buzz, кратных и тому и другому — FizzBuzz.")
                .language("Python").difficulty(InterviewQuestion.Difficulty.EASY).build());
        questions.save(InterviewQuestion.builder()
                .title("Sum of Array").titleRu("Сумма массива")
                .prompt("Write a function sum(arr) returning the sum of all integers in arr.")
                .promptRu("Напишите функцию sum(arr), возвращающую сумму всех целых чисел в arr.")
                .language("Python").difficulty(InterviewQuestion.Difficulty.EASY).build());
        questions.save(InterviewQuestion.builder()
                .title("Palindrome Check").titleRu("Проверка палиндрома")
                .prompt("Return true if the input string reads the same forwards and backwards (ignore case).")
                .promptRu("Верните true, если строка читается одинаково в обе стороны (без учёта регистра).")
                .language("Java").difficulty(InterviewQuestion.Difficulty.EASY).build());
        questions.save(InterviewQuestion.builder()
                .title("Count Vowels").titleRu("Количество гласных")
                .prompt("Return the number of vowels (a,e,i,o,u) in the input string.")
                .promptRu("Верните количество гласных (a,e,i,o,u) во входной строке.")
                .language("Go").difficulty(InterviewQuestion.Difficulty.EASY).build());
    }
}
