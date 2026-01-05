# [MangaWatch](https://manga-watch.vercel.app/) 

## What is MangaWatch?

Full-stack manga tracking application inspired by Letterboxd/MAL.
Choose from a massive selection of Mangas in the Discover section, pick what titles you like and add them to your library.
Track your progress of that title in the Library.

**Live Demo:** https://manga-watch.vercel.app/  
**Note:** Backend cold starts may take ~50 seconds on first request (Render free tier).

## Features

- **Massive Catalog:** Browse 87,000+ manga titles imported from MangaDex
- **Personal Library:** Track reading status (Reading, Plan to Read, Completed)
- **Advanced Search:** Filter by genre, sort by title/author/date, paginated results
- **Secure Authentication:** JWT-based auth with protected routes
- **Responsive Design:** Mobile-friendly UI with light/dark mode
- **Resume-Safe Imports:** Batch import system with cursor-based resumption

## Tech Stack

**Frontend:** React (Vite), Tailwind CSS, Axios  
**Backend:** Spring Boot, Spring Data JPA, JWT Authentication, Flyway  
**Database:** PostgreSQL (Docker)  
**External API:** MangaDex API

## Quick Start

### Prerequisites
- Node.js 18+
- Java 17
- Docker Desktop
- MangaDex API credentials ([create here](https://api.mangadex.org/docs/02-authentication/personal-clients/))

### 1. Clone and Setup Frontend
```bash
git clone https://github.com/Rjv-RY/MangaWatch
cd MangaWatch/frontend
npm install
npm run dev
```

### 2. Setup Backend

See [Backend README](./backend/README.md) for detailed instructions including:
- Database container setup
- Environment variable configuration
- MangaDex API integration
- Import process

## Project Structure
```
MangaWatch/
├── frontend/          # React application
│   ├── src/
│   └── README.md
└── backend/           # Spring Boot API
    ├── src/
    └── README.md      # Detailed backend setup
```
## Snippets

**PC View**

![PC View](https://github.com/Rjv-RY/MangaWatch/blob/main/frontend/src/snipsForGitHub/Discover.png)

**Mobile View (Navbar items become dowpdown)**

![Mobile View](https://github.com/Rjv-RY/MangaWatch/blob/main/frontend/src/snipsForGitHub/MobileView.png)

_(More Screenshots in [Frontend README](./frontend/README.md))_

## Deployment

- **Frontend:** Vercel
- **Backend:** Render (free tier)
- **Database:** Render PostgreSQL

### Backend Setup continues in the Backend's Readme due to its complicated nature.

## Acknowledgments

Built with the [MangaDex API](https://api.mangadex.org/).

## Possible Future Features
- User ratings and reviews
- Reading lists and recommendations
- Social features (follow users, share libraries)
