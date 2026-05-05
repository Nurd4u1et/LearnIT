-- ============================================================================
-- Seed data: courses, lessons, interview questions, demo admin user.
-- Run AFTER schema.sql.
-- ============================================================================

-- Demo admin (password: Admin123!) -- BCrypt hash precomputed
INSERT INTO users (name, email, phone, password_hash, role, plan, language, email_verified)
VALUES ('Admin', '[email protected]', '+10000000000',
        '$2a$10$wH8Q5Lb1eQpQ1mJ6X8mGyu9b1mYbA4kHc1jKZl1m3o2yJ0rQyZb6m',
        'ADMIN', 'PRO', 'en', TRUE)
ON CONFLICT DO NOTHING;

-- ---------------- COURSES ----------------
INSERT INTO courses (slug, title, title_ru, description, description_ru, language, level, cover_url) VALUES
 ('python-basics',
  'Python Basics',         'Основы Python',
  'Start your programming journey with Python — variables, control flow, and your first real programs.',
  'Начните свой путь в программировании с Python — переменные, управляющие конструкции и первые программы.',
  'Python', 'BEGINNER',
  'https://images.unsplash.com/photo-1526379095098-d400fd0bf935?w=1200'),
 ('java-basics',
  'Java Foundations',      'Основы Java',
  'A practical introduction to Java — syntax, types, and writing your first class.',
  'Практическое введение в Java — синтаксис, типы и первый класс.',
  'Java', 'BEGINNER',
  'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=1200'),
 ('go-basics',
  'Go Foundations',        'Основы Go',
  'Learn Go from the ground up — packages, functions, and idiomatic style.',
  'Изучайте Go с нуля — пакеты, функции и идиоматический стиль.',
  'Go',   'BEGINNER',
  'https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=1200');

-- ---------------- LESSONS ----------------
-- Python: 2 lessons
INSERT INTO lessons (course_id, position, title, title_ru, video_url, description, description_ru, task, task_ru, expected_solution)
VALUES
 ((SELECT id FROM courses WHERE slug='python-basics'), 1,
  'Hello, Python', 'Привет, Python',
  'https://www.youtube.com/embed/kqtD5dpn9C8',
  'In Python, printing to the console is done with the built-in print() function. Strings can be wrapped in single or double quotes. This is the universal "first program" in any language.',
  'В Python вывод в консоль выполняется встроенной функцией print(). Строки можно заключать в одинарные или двойные кавычки. Это универсальная «первая программа» в любом языке.',
  'Write a program that prints exactly: Hello, World!',
  'Напишите программу, которая выводит ровно: Hello, World!',
  'print("Hello, World!")'),
 ((SELECT id FROM courses WHERE slug='python-basics'), 2,
  'Variables and Types', 'Переменные и типы',
  'https://www.youtube.com/embed/cQT33yu9pY8',
  'Variables in Python are dynamically typed. Common built-in types include int, float, str, and bool. You can inspect a value''s type with the type() function.',
  'Переменные в Python имеют динамическую типизацию. Среди встроенных типов: int, float, str, bool. Тип значения можно проверить функцией type().',
  'Create a variable name with your name and age with your age (an integer), then print: "I am {name} and I am {age} years old".',
  'Создайте переменную name с вашим именем и age с вашим возрастом (целое), затем выведите: "I am {name} and I am {age} years old".',
  'name = "Ada"\nage = 30\nprint(f"I am {name} and I am {age} years old")');

-- Java: 1 lesson
INSERT INTO lessons (course_id, position, title, title_ru, video_url, description, description_ru, task, task_ru, expected_solution)
VALUES
 ((SELECT id FROM courses WHERE slug='java-basics'), 1,
  'Your First Java Class', 'Первый класс на Java',
  'https://www.youtube.com/embed/eIrMbAQSU34',
  'Every Java program starts inside a class. The entry point is the public static void main(String[] args) method. System.out.println prints a line to the console.',
  'Каждая программа на Java начинается внутри класса. Точка входа — метод public static void main(String[] args). System.out.println печатает строку в консоль.',
  'Write a class Main with a main method that prints exactly: Hello, Java!',
  'Напишите класс Main с методом main, который выводит ровно: Hello, Java!',
  'public class Main {\n  public static void main(String[] args) {\n    System.out.println("Hello, Java!");\n  }\n}');

-- Go: 1 lesson
INSERT INTO lessons (course_id, position, title, title_ru, video_url, description, description_ru, task, task_ru, expected_solution)
VALUES
 ((SELECT id FROM courses WHERE slug='go-basics'), 1,
  'Hello from Go', 'Привет от Go',
  'https://www.youtube.com/embed/YS4e4q9oBaU',
  'A Go program belongs to a package. Executable programs use package main and a func main() entry point. fmt.Println writes a line to standard output.',
  'Программа на Go принадлежит пакету. Исполняемые программы используют package main и точку входа func main(). fmt.Println печатает строку в стандартный вывод.',
  'Write a Go program that prints exactly: Hello, Go!',
  'Напишите программу на Go, которая выводит ровно: Hello, Go!',
  'package main\nimport "fmt"\nfunc main() { fmt.Println("Hello, Go!") }');

-- ---------------- INTERVIEW QUESTIONS ----------------
INSERT INTO interview_questions (title, title_ru, prompt, prompt_ru, language, difficulty) VALUES
 ('Reverse a String', 'Перевернуть строку',
  'Write a function reverse(s) that returns the input string s reversed.',
  'Напишите функцию reverse(s), которая возвращает входную строку s в обратном порядке.',
  'Python', 'EASY'),
 ('FizzBuzz', 'FizzBuzz',
  'Print numbers 1..15. For multiples of 3 print Fizz, of 5 print Buzz, of both print FizzBuzz.',
  'Выведите числа 1..15. Для кратных 3 — Fizz, кратных 5 — Buzz, кратных и тому и другому — FizzBuzz.',
  'Python', 'EASY'),
 ('Sum of Array', 'Сумма массива',
  'Write a function sum(arr) returning the sum of all integers in arr.',
  'Напишите функцию sum(arr), возвращающую сумму всех целых чисел в arr.',
  'Python', 'EASY'),
 ('Palindrome Check', 'Проверка палиндрома',
  'Return true if the input string reads the same forwards and backwards (ignore case).',
  'Верните true, если строка читается одинаково в обе стороны (без учёта регистра).',
  'Java', 'EASY'),
 ('Count Vowels', 'Количество гласных',
  'Return the number of vowels (a,e,i,o,u) in the input string.',
  'Верните количество гласных (a,e,i,o,u) во входной строке.',
  'Go', 'EASY');
