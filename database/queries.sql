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


ALTER TABLE "Record"
ADD COLUMN "Valid" BOOLEAN NOT NULL;

-- March 17th 2025
ALTER TABLE "Record" ALTER COLUMN sessionid TYPE BIGINT USING sessionid::BIGINT;

-- March 22nd 2025
CREATE TABLE Session (
    SessionID INT PRIMARY KEY,
    SessionName VARCHAR(255),
    initialLength FLOAT,
    initialArea FLOAT,
    TestType INT
);

-- March 23rd 2025
ALTER TABLE "Record" 
DROP COLUMN materialid;

ALTER TABLE Session
ADD COLUMN datecreated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN datemodified TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE session
ALTER COLUMN sessionid TYPE BIGINT;

ALTER TABLE "Record" 
ADD CONSTRAINT fk_record_session
FOREIGN KEY (sessionid)
REFERENCES session(sessionid)
ON DELETE CASCADE;

-- March 26th 2025
ALTER TABLE Session
ADD COLUMN yieldstress FLOAT NULL,
ADD COLUMN yieldstrain FLOAT NULL;