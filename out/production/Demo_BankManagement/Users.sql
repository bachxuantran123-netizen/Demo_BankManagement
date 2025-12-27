USE BankDB;
GO

IF OBJECT_ID('dbo.Users', 'U') IS NOT NULL DROP TABLE dbo.Users;
GO

CREATE TABLE Users (
    username VARCHAR(50) PRIMARY KEY,
    password VARCHAR(50) NOT NULL,
    full_name NVARCHAR(100)
);

INSERT INTO Users VALUES ('admin', '123456', N'Quản Trị Viên');
GO