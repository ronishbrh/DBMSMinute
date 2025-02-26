create database minute;
use minute;

CREATE TABLE User(
	userID INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    committeeName VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE Committee (
    committee_ID INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE, 
    description TEXT,
    date_of_creation DATE
);


CREATE TABLE Member(
	member_ID INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL,
    address VARCHAR(255),
    email VARCHAR(255) UNIQUE,
    phone_no VARCHAR(25),
    date_of_join DATE 
);





CREATE TABLE belongs_to (
    committee_id INT,
    member_id INT,
    PRIMARY KEY (committee_id, member_id),
    FOREIGN KEY (committee_id) REFERENCES committee(committee_id) ON DELETE CASCADE,
    FOREIGN KEY (member_id) REFERENCES member(member_id) ON DELETE CASCADE
);

CREATE TABLE meeting(
	meeting_id INT AUTO_INCREMENT,
    committee_name VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
	meeting_date Date NOT NULL,
    meeting_time Time,
    location VARCHAR(255),
    meeting_type VARCHAR(255),
    PRIMARY KEY(meeting_id, title),
    FOREIGN KEY(committee_name) REFERENCES committee(name) 
);

CREATE TABLE attends(
    meeting_id INT,
    member_id INT,
    PRIMARY KEY (meeting_id, member_id),
    FOREIGN KEY (meeting_id) REFERENCES meeting(meeting_id) ON DELETE CASCADE,
    FOREIGN KEY (member_id) REFERENCES member(member_id) ON DELETE CASCADE
);


DELIMITER //

CREATE TRIGGER add_new_member_to_meetings
AFTER INSERT ON belongs_to
FOR EACH ROW
BEGIN
    INSERT INTO attends (meeting_id, member_id)
    SELECT meeting.meeting_id, NEW.member_id
    FROM meeting
    WHERE meeting.committee_name = (SELECT committee_name FROM committee WHERE committee_id = NEW.committee_id);
END;
//

DELIMITER ;


CREATE TABLE agenda (
    agenda_id INT NOT NULL AUTO_INCREMENT,
    topic VARCHAR(255) NOT NULL,
    meeting_id INT NOT NULL,
    time_slot VARCHAR(50),
    presenter VARCHAR(255),
    PRIMARY KEY (agenda_id, topic),
    FOREIGN KEY (meeting_id) REFERENCES meeting(meeting_id) ON DELETE CASCADE
);

CREATE TABLE document (
    document_id INT AUTO_INCREMENT PRIMARY KEY,
    agenda_id INT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_data LONGBLOB NOT NULL,
    type VARCHAR(50) GENERATED ALWAYS AS (SUBSTRING_INDEX(file_name, '.', -1)) STORED,
    FOREIGN KEY (agenda_id) REFERENCES agenda(agenda_id) ON DELETE CASCADE
);




CREATE TABLE present_attendance(
    meeting_id INT,
    member_id INT,
    PRIMARY KEY (meeting_id, member_id),
    FOREIGN KEY (meeting_id) REFERENCES meeting(meeting_id) ON DELETE CASCADE,
    FOREIGN KEY (member_id) REFERENCES member(member_id) ON DELETE CASCADE
);

CREATE TABLE discussion(
    discussion_id INT AUTO_INCREMENT,
    agenda_id INT NOT NULL,
    description VARCHAR(255) NOT NULL,
    PRIMARY KEY (discussion_id),
    FOREIGN KEY (agenda_id) REFERENCES agenda(agenda_id) ON DELETE CASCADE
);

CREATE TABLE follow_up(
    follow_up_id INT AUTO_INCREMENT,
    discussion_id INT NOT NULL,
    description VARCHAR(255) ,
    status VARCHAR(25) NOT NULL,
    notes VARCHAR(255),
    PRIMARY KEY (follow_up_id),
    FOREIGN KEY (discussion_id) REFERENCES discussion(discussion_id) ON DELETE CASCADE
);

CREATE TABLE minute(
	minute_id INT AUTO_INCREMENT,
    meeting_id INT NOT NULL,
    content VARCHAR(500) NOT NULL,
    created_by VARCHAR(50) NOT NULL,
    creation_time DATETIME NOT NULL,
    updated_time DATETIME,
    approved_by VARCHAR(50) NOT NULL,
    PRIMARY KEY(minute_id),
    FOREIGN KEY(meeting_id) REFERENCES meeting(meeting_id) ON DELETE CASCADE
);
    





    
    