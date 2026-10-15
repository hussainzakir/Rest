-- Fixture based on the repository CensusControllerIT fixture, with isolated IDs.
INSERT INTO prospect (id, company_name, hq_address, hq_city, hq_state, hq_postal_code, sales_naics_code, primary_naics_code, benefit_start_date, default_group_id, age_calculation_date, co_company_code, data_setup_status) values ('Census-Service-IT-P1','Census Service IT','Almyra St','Arkansas','AR','72003','354','No',now(),null,null,'D2S',FALSE);

INSERT INTO prospect_employee values ('Census-Service-IT-E1','Census-Service-IT-P1','Chris','Evans',STR_TO_DATE('1990-06-25','%Y-%m-%d'),'M','AR','72003','AR',2120.20,'Manager',TRUE,"81234");

INSERT INTO ben_group values (19015, 'Census-Service-IT-P1', 'Integration Group','STD');
INSERT INTO ee_group values (19015,'Census-Service-IT-E1');

INSERT INTO prospect_ee_covg_detail values ('Census-Service-IT-E1','10','EE');
INSERT INTO prospect_ee_covg_detail values ('Census-Service-IT-E1','11','EC');
INSERT INTO prospect_ee_covg_detail values ('Census-Service-IT-E1','14','EF');

INSERT INTO prospect_plan values (1,'Census-Service-IT-P1','10',null,null);
INSERT INTO prospect_plan values (2,'Census-Service-IT-P1','11',null,25);
INSERT INTO prospect_plan values (4,'Census-Service-IT-P1','14',null,null);

-- Required for CensusServiceImpl.buildCensusInfoResponse(): plan assignments are
-- derived by matching employee plans to the employee's coverage detail rows.
INSERT INTO prospect_employee_plan (prospect_ee_id, benefit_plan_id)
VALUES ('Census-Service-IT-E1', 1);
INSERT INTO prospect_employee_plan (prospect_ee_id, benefit_plan_id)
VALUES ('Census-Service-IT-E1', 2);
INSERT INTO prospect_employee_plan (prospect_ee_id, benefit_plan_id)
VALUES ('Census-Service-IT-E1', 4);
