USE BankDB;
GO

CREATE TABLE Accounts (
    id INT IDENTITY(1,1) PRIMARY KEY,
    account_code VARCHAR(10) UNIQUE NOT NULL,
    owner_name NVARCHAR(100) NOT NULL,
    creation_date DATE,
    type VARCHAR(20),
    
    -- Savings
    deposit_amount FLOAT,
    deposit_date DATE,
    interest_rate FLOAT,
    term INT,
    
    -- Payment
    card_number VARCHAR(20),
    balance FLOAT
);
GO