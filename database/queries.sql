-- Major queries ran on the database for documentation purposes

--March 8th 2025
CREATE TABLE "User" (
    UserID SERIAL PRIMARY KEY,
    Username VARCHAR(50) UNIQUE NOT NULL,
    FirstName VARCHAR(50) NOT NULL,
    LastName VARCHAR(50) NOT NULL,
    PasswordHash TEXT NOT NULL,
    ProfilePictureURL TEXT,
    DateCreated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    DateModified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE "Material" (
    MaterialID SERIAL PRIMARY KEY,
    Name VARCHAR(100) UNIQUE NOT NULL,
    PhotoURL TEXT,
    DateCreated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    DateModified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE "Record" (
    RecordID SERIAL PRIMARY KEY,
    Distance FLOAT NOT NULL,
    Temperature FLOAT NOT NULL,
    Pressure FLOAT NOT NULL,
    MaterialID INT NOT NULL,
    DateCreated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    DateModified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_material FOREIGN KEY (MaterialID) REFERENCES "Material"(MaterialID) ON DELETE CASCADE
);

-- March 15th 2025
ALTER TABLE "Record"
ADD COLUMN SessionID INT NULL,
ADD COLUMN "Timestamp" TIMESTAMP NOT NULL;