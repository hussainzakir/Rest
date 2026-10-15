package com.trinet.bss.prospect.service.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.clearInvocations;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.bss.prospect.dto.response.CensusInfoRes;
import com.trinet.bss.prospect.dto.response.CensusRes;
import com.trinet.bss.prospect.dto.response.EmployeePlansRes;
import com.trinet.bss.prospect.dto.request.EmployeeCoverageTierReq;
import com.trinet.bss.prospect.service.BenefitEstimateRecalcService;
import com.trinet.bss.prospect.repository.ProspectEECovgDetailRepository;
import com.trinet.bss.prospect.model.ProspectEECovgDetail;
import com.trinet.bss.prospect.model.ProspectEmployee;
import com.trinet.bss.prospect.service.CensusService;

/**
 * Integration tests for CensusServiceImpl using the real Spring context and database.
 * The base prospect/employee data is loaded from the SQL fixture for each test.
 */
@Tag("integration")
@SpringBootTest
@Transactional
@Sql("/db-test/data/CensusServiceImplIT.sql")
class CensusServiceImplIT {

    private static final String PROSPECT_ID = "Census-Service-IT-P1";
    private static final String EMPLOYEE_ID = "Census-Service-IT-E1";
    private static final int GROUP_ID = 19015;

    @Autowired
    private CensusService censusService;

    @Autowired
    private ProspectEECovgDetailRepository coverageRepository;

    @MockBean
    private BenefitEstimateRecalcService benefitEstimateRecalcService;

    @Test
    @DisplayName("Returns census data for a prospect with an employee")
    void getCensusDataBy_returnsEmployee() {
        List<CensusRes> result = censusService.getCensusDataBy(PROSPECT_ID);

        assertEquals(1, result.size());
        assertEquals(EMPLOYEE_ID, result.get(0).getEmployeeId());
        assertEquals("Chris Evans", result.get(0).getEmployeeName());
    }

    @Test
    @DisplayName("Returns an empty census list when no employees exist for a prospect")
    void getCensusDataBy_returnsEmptyListForUnknownProspect() {
        assertTrue(censusService.getCensusDataBy("Census-Service-IT-MISSING").isEmpty());
    }

    @Test
    @DisplayName("Returns employees associated with a prospect")
    void getProspectEmployeesBy_returnsEmployees() {
        List<ProspectEmployee> result = censusService.getProspectEmployeesBy(PROSPECT_ID);

        assertEquals(1, result.size());
        assertEquals(EMPLOYEE_ID, result.get(0).getId());
    }

    @Test
    @DisplayName("Returns an empty employee list for an unknown prospect")
    void getProspectEmployeesBy_returnsEmptyListForUnknownProspect() {
        assertTrue(censusService.getProspectEmployeesBy("Census-Service-IT-MISSING").isEmpty());
    }

    @Test
    @DisplayName("Returns the prospect ID for an existing employee")
    void getProspectId_returnsProspectId() {
        assertEquals(PROSPECT_ID, censusService.getProspectId(EMPLOYEE_ID));
    }

    @Test
    @DisplayName("Returns an empty string when the employee does not exist")
    void getProspectId_returnsEmptyStringForUnknownEmployee() {
        assertEquals("", censusService.getProspectId("Census-Service-IT-MISSING"));
    }

    @Test
    @DisplayName("Returns census information for an existing prospect")
    void getCensusInfoByProspect_returnsEmployeeInfo() {
        List<CensusInfoRes> result = censusService.getCensusInfoBy(PROSPECT_ID);

        assertEquals(1, result.size());
        assertNotNull(result.get(0));
    }

    @Test
    @DisplayName("Returns an empty census information list for a prospect without employees")
    void getCensusInfoByProspect_returnsEmptyListForUnknownProspect() {
        assertTrue(censusService.getCensusInfoBy("Census-Service-IT-MISSING").isEmpty());
    }

    @Test
    @DisplayName("Returns census information for an existing employee ID")
    void getCensusInfoByEmployeeIds_returnsEmployeeInfo() {
        List<CensusInfoRes> result = censusService.getCensusInfoBy(List.of(EMPLOYEE_ID));

        assertEquals(1, result.size());
        assertNotNull(result.get(0));
    }

    @Test
    @DisplayName("Returns an empty census information list for unknown employee IDs")
    void getCensusInfoByEmployeeIds_returnsEmptyListForUnknownEmployee() {
        assertTrue(censusService.getCensusInfoBy(List.of("Census-Service-IT-MISSING")).isEmpty());
    }

    @Test
    @DisplayName("Returns employee plan data for a prospect")
    void getEmployeesPlansBy_returnsEmployeePlanResponse() {
        List<EmployeePlansRes> result = censusService.getEmployeesPlansBy(PROSPECT_ID);

        assertEquals(1, result.size());
        assertEquals(EMPLOYEE_ID, result.get(0).getEmployeeId());
        assertNotNull(result.get(0).getBenefitPlans());
    }

    @Test
    @DisplayName("Returns an empty employee plan list for a prospect without employees")
    void getEmployeesPlansBy_returnsEmptyListForUnknownProspect() {
        assertTrue(censusService.getEmployeesPlansBy("Census-Service-IT-MISSING").isEmpty());
    }

    @Test
    @DisplayName("Returns employee IDs for a benefit group")
    void findEmployeeIdsBy_returnsEmployeeIds() {
        List<String> result = censusService.findEmployeeIdsBy(GROUP_ID);

        assertFalse(result.isEmpty());
        assertTrue(result.contains(EMPLOYEE_ID));
    }

    @Test
    @DisplayName("Returns prospect IDs associated with employee IDs")
    void findProspectIdsByEmployeeIds_returnsProspectIds() {
        List<String> result = censusService.findProspectIdsByEmployeeIds(List.of(EMPLOYEE_ID));

        assertTrue(result.contains(PROSPECT_ID));
    }

    @Test
    @DisplayName("Updates existing dental and vision tiers and requests estimate recalculation")
    void saveEmployeeCoverageTiers_updatesExistingCoverageRows() {
        clearInvocations(benefitEstimateRecalcService);
        List<EmployeeCoverageTierReq> request = List.of(EmployeeCoverageTierReq.builder()
                .employeeId(EMPLOYEE_ID).dentalTier("C").visionTier("F").build());

        censusService.saveEmployeeCoverageTiers(PROSPECT_ID, request);

        List<ProspectEECovgDetail> saved = coverageRepository.getAllByEmployeeId(List.of(EMPLOYEE_ID));
        assertEquals("C", saved.stream()
                .filter(row -> "11".equals(row.getProspectEECovgDetailPK().getBenefitTypeCode()))
                .findFirst().orElseThrow().getCoverageLevelCode());
        assertEquals("F", saved.stream()
                .filter(row -> "14".equals(row.getProspectEECovgDetailPK().getBenefitTypeCode()))
                .findFirst().orElseThrow().getCoverageLevelCode());
        verify(benefitEstimateRecalcService).forCensusChange(List.of(EMPLOYEE_ID));
    }

    @Test
    @DisplayName("Returns no age-banded medical employees when the prospect has no matching plan")
    void findMedicalAgeBandedEmployees_returnsEmptyListWhenNoMatchingPlan() {
        assertTrue(censusService.findMedicalAgeBandedEmployees("Census-Service-IT-MISSING").isEmpty());
    }

    @Test
    @DisplayName("Does nothing when asked to save an empty employee-plan list")
    void saveUpdateEmployeePlans_ignoresEmptyList() {
        assertDoesNotThrow(() -> censusService.saveUpdateEmployeePlans(List.of()));
    }

}
