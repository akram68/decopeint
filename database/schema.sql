
-- ========================================================
-- 2️⃣ TABLE CLIENT
-- ========================================================
CREATE TABLE client (
    id_client INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    telephone VARCHAR(20),
    email VARCHAR(100),
    adresse TEXT
);

-- ========================================================
-- 3️⃣ TABLE FOURNISSEUR
-- ========================================================
CREATE TABLE fournisseur (
    id_fournisseur INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    telephone VARCHAR(20),
    email VARCHAR(100),
    adresse TEXT
);

-- ========================================================
-- 4️⃣ TABLE TYPE_SERVICE
-- ========================================================
CREATE TABLE type_service (
    id_type_service INT AUTO_INCREMENT PRIMARY KEY,
    nom_type VARCHAR(100) NOT NULL UNIQUE
);

-- Example static data
INSERT INTO type_service (nom_type) VALUES 
('Impression'), 
('Panneau publicitaire'), 
('Sérigraphie');

-- ========================================================
-- 5️⃣ TABLE SERVICE
-- ========================================================
CREATE TABLE service (
    id_service INT AUTO_INCREMENT PRIMARY KEY,
    id_client INT NOT NULL,
    id_type_service INT NOT NULL,
    description TEXT,
    date_creation DATETIME DEFAULT CURRENT_TIMESTAMP,
    statut ENUM('EN_ATTENTE','EN_COURS','TERMINE') DEFAULT 'EN_ATTENTE',
    prix_total DECIMAL(12,2) NOT NULL,
    FOREIGN KEY (id_client) REFERENCES client(id_client) ON DELETE CASCADE,
    FOREIGN KEY (id_type_service) REFERENCES type_service(id_type_service)
);

-- ========================================================
-- 6️⃣ TABLE PAIEMENT_VENTE
-- ========================================================
CREATE TABLE paiement_vente (
    id_paiement INT AUTO_INCREMENT PRIMARY KEY,
    id_service INT NOT NULL,
    date_paiement DATETIME DEFAULT CURRENT_TIMESTAMP,
    montant DECIMAL(12,2) NOT NULL,
    mode_paiement VARCHAR(30),
    FOREIGN KEY (id_service) REFERENCES service(id_service) ON DELETE CASCADE
);

-- ========================================================
-- 7️⃣ TABLE ACHAT
-- ========================================================
CREATE TABLE achat (
    id_achat INT AUTO_INCREMENT PRIMARY KEY,
    id_fournisseur INT NOT NULL,
    date_achat DATETIME DEFAULT CURRENT_TIMESTAMP,
    description TEXT,
    prix_total DECIMAL(12,2) NOT NULL,
    statut ENUM('EN_ATTENTE','EN_COURS','PAYE') DEFAULT 'EN_ATTENTE',
    FOREIGN KEY (id_fournisseur) REFERENCES fournisseur(id_fournisseur)
);

-- ========================================================
-- 8️⃣ TABLE PAIEMENT_ACHAT
-- ========================================================
CREATE TABLE paiement_achat (
    id_paiement_achat INT AUTO_INCREMENT PRIMARY KEY,
    id_achat INT NOT NULL,
    date_paiement DATETIME DEFAULT CURRENT_TIMESTAMP,
    montant DECIMAL(12,2) NOT NULL,
    mode_paiement VARCHAR(30),
    FOREIGN KEY (id_achat) REFERENCES achat(id_achat) ON DELETE CASCADE
);

-- ========================================================
-- 9️⃣ TABLE BON
-- ========================================================
CREATE TABLE bon (
    id_bon INT AUTO_INCREMENT PRIMARY KEY,
    type_operation ENUM('VENTE','ACHAT') NOT NULL,
    id_operation INT NOT NULL,
    date_bon DATETIME DEFAULT CURRENT_TIMESTAMP,
    montant DECIMAL(12,2),
    remarque TEXT
);

-- ========================================================
-- 10️⃣ Example static data for testing
-- ========================================================

-- Clients
INSERT INTO client (nom, telephone, email, adresse) VALUES
('Alice Dupont','0612345678','alice@example.com','123 Rue A'),
('Bob Martin','0698765432','bob@example.com','456 Rue B');

-- Fournisseurs
INSERT INTO fournisseur (nom, telephone, email, adresse) VALUES
('Fournisseur 1','0611111111','fourn1@example.com','789 Rue C'),
('Fournisseur 2','0622222222','fourn2@example.com','101 Rue D');

-- Services
INSERT INTO service (id_client, id_type_service, description, statut, prix_total) VALUES
(1, 1, 'Impression flyers 1000 pcs', 'EN_ATTENTE', 5000),
(2, 2, 'Panneau publicitaire 2x3m', 'EN_COURS', 12000);

-- Payments for services
INSERT INTO paiement_vente (id_service, montant, mode_paiement) VALUES
(1, 2000, 'Cash'),
(1, 3000, 'Virement'),
(2, 12000, 'Chèque');

-- Purchases
INSERT INTO achat (id_fournisseur, description, prix_total, statut) VALUES
(1, 'Papier pour flyers', 3000, 'EN_COURS'),
(2, 'Encre pour panneaux', 2000, 'PAYE');

-- Payments for purchases
INSERT INTO paiement_achat (id_achat, montant, mode_paiement) VALUES
(1, 1000, 'Cash'),
(1, 2000, 'Virement'),
(2, 2000, 'Chèque');

-- Bons (vouchers)
INSERT INTO bon (type_operation, id_operation, montant, remarque) VALUES
('VENTE',1,5000,'Payment for flyers'),
('ACHAT',1,3000,'Purchase of paper');
