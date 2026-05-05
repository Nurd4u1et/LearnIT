export type CourseSummary = {
  id: number; slug: string; title: string; description: string;
  language: string; level: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';
  coverUrl: string | null; proOnly: boolean;
  avgRating: number; lessonCount: number;
};

export type LessonSummary = {
  id: number; position: number; title: string; completed: boolean;
};

export type CourseDetail = {
  summary: CourseSummary;
  lessons: LessonSummary[];
  progressPercent: number;
  enrolled: boolean;
};

export type EnrolledCourse = {
  course: CourseSummary;
  progressPercent: number;
  lessonsCompleted: number;
  lessonsTotal: number;
};

export type LessonDetail = {
  id: number; courseId: number; position: number;
  title: string; description: string; task: string;
  videoUrl: string | null; completed: boolean;
};

export type Submission = {
  id: number;
  status: 'SUBMITTED' | 'PASSED' | 'FAILED';
  content: string;
  feedback: string | null;
  createdAt: string;
};

export type Comment = {
  id: number; userId: number; userName: string;
  parentId: number | null; body: string; createdAt: string;
};

export type Review = {
  id: number; userId: number; userName: string;
  rating: number; body: string | null; createdAt: string;
};

export type AiMessage = {
  id: number; role: 'USER' | 'ASSISTANT' | 'SYSTEM';
  content: string; createdAt: string;
};

export type InterviewQuestion = {
  id: number; title: string; prompt: string;
  language: string; difficulty: string;
};

export type InterviewStart = {
  interviewId: number;
  questions: InterviewQuestion[];
  durationMin: number;
};

export type InterviewFinish = {
  interviewId: number; score: number; total: number;
  warnings: number; status: 'FINISHED' | 'TERMINATED' | 'IN_PROGRESS';
};

export type Notification = {
  id: number; type: string; title: string; body: string | null;
  link: string | null; read: boolean; createdAt: string;
};
