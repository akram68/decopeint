DROP DATABASE IF EXISTS gestion_publicite;
CREATE DATABASE gestion_publicite;
USE gestion_publicite;

-- ========================================================
-- CLIENT
-- ========================================================
CREATE TABLE client (
    id_client INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    telephone VARCHAR(20),
    email VARCHAR(100),
    adresse TEXT
);

-- ========================================================
-- FOURNISSEUR
-- ========================================================
CREATE TABLE fournisseur (
    id_fournisseur INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    telephone VARCHAR(20),
    email VARCHAR(100),
    adresse TEXT
);

-- ========================================================
-- TYPE SERVICE
-- ========================================================
CREATE TABLE type_service (
    id_type_service INT AUTO_INCREMENT PRIMARY KEY,
    nom_type VARCHAR(100) UNIQUE NOT NULL
);

INSERT INTO type_service (nom_type) VALUES
('Impression'),
('Panneau publicitaire'),
('Sérigraphie');

-- ========================================================
-- SERVICE (VENTE)
-- ========================================================
CREATE TABLE service (
    id_service INT AUTO_INCREMENT PRIMARY KEY,
    id_client INT NOT NULL,
    id_type_service INT NOT NULL,
    description TEXT,
    date_creation DATETIME DEFAULT CURRENT_TIMESTAMP,

    prix_total DECIMAL(12,2) NOT NULL,
    montant_paye DECIMAL(12,2) DEFAULT 0,
    reste_a_payer DECIMAL(12,2) NOT NULL,

    statut_paiement ENUM('NON_PAYE','PARTIEL','PAYE') DEFAULT 'NON_PAYE',

    FOREIGN KEY (id_client) REFERENCES client(id_client),
    FOREIGN KEY (id_type_service) REFERENCES type_service(id_type_service)
);

-- ========================================================
-- PAIEMENT SERVICE (TRANCHES)
-- ========================================================
CREATE TABLE paiement_service (
    id_paiement INT AUTO_INCREMENT PRIMARY KEY,
    id_service INT NOT NULL,
    date_paiement DATETIME DEFAULT CURRENT_TIMESTAMP,
    montant DECIMAL(12,2) NOT NULL,
    mode_paiement VARCHAR(30),
    FOREIGN KEY (id_service) REFERENCES service(id_service) ON DELETE CASCADE
);

-- ========================================================
-- ACHAT (MÊME LOGIQUE QUE SERVICE)
-- ========================================================
CREATE TABLE achat (
    id_achat INT AUTO_INCREMENT PRIMARY KEY,
    id_fournisseur INT NOT NULL,
    description TEXT,
    date_achat DATETIME DEFAULT CURRENT_TIMESTAMP,

    prix_total DECIMAL(12,2) NOT NULL,
    montant_paye DECIMAL(12,2) DEFAULT 0,
    reste_a_payer DECIMAL(12,2) NOT NULL,

    statut_paiement ENUM('NON_PAYE','PARTIEL','PAYE') DEFAULT 'NON_PAYE',

    FOREIGN KEY (id_fournisseur) REFERENCES fournisseur(id_fournisseur)
);

-- ========================================================
-- PAIEMENT ACHAT
-- ========================================================
CREATE TABLE paiement_achat (
    id_paiement INT AUTO_INCREMENT PRIMARY KEY,
    id_achat INT NOT NULL,
    date_paiement DATETIME DEFAULT CURRENT_TIMESTAMP,
    montant DECIMAL(12,2) NOT NULL,
    mode_paiement VARCHAR(30),
    FOREIGN KEY (id_achat) REFERENCES achat(id_achat) ON DELETE CASCADE
);

-- ========================================================
-- BON (VENTE / ACHAT)
-- ========================================================
CREATE TABLE bon (
    id_bon INT AUTO_INCREMENT PRIMARY KEY,
    type_bon ENUM('SERVICE','ACHAT') NOT NULL,
    id_reference INT NOT NULL,
    date_bon DATETIME DEFAULT CURRENT_TIMESTAMP,
    montant DECIMAL(12,2),
    remarque TEXT
);
