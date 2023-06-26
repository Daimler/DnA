/* LICENSE START
 * 
 * MIT License
 * 
 * Copyright (c) 2019 Daimler TSS GmbH
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * LICENSE END 
 */

package com.daimler.data.service.report;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.lang.reflect.Type;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.daimler.data.application.auth.UserStore;
import com.daimler.data.assembler.ReportAssembler;
import com.daimler.data.auth.client.DnaAuthClient;
import com.daimler.data.auth.client.UsersCollection;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.db.entities.ReportNsql;
import com.daimler.data.db.jsonb.report.DataSource;
import com.daimler.data.db.jsonb.report.DataWarehouse;
import com.daimler.data.db.jsonb.report.Division;
import com.daimler.data.db.jsonb.report.InternalCustomer;
import com.daimler.data.db.jsonb.report.KPI;
import com.daimler.data.db.jsonb.report.KPIName;
import com.daimler.data.db.jsonb.report.Report;
import com.daimler.data.db.jsonb.report.SingleDataSource;
import com.daimler.data.db.jsonb.report.Subdivision;
import com.daimler.data.db.jsonb.report.TeamMember;
import com.daimler.data.db.repo.report.ReportCustomRepository;
import com.daimler.data.db.repo.report.ReportRepository;
import com.daimler.data.dto.KpiName.KpiNameVO;
import com.daimler.data.dto.dataSource.DataSourceBulkRequestVO;
import com.daimler.data.dto.dataSource.DataSourceCreateVO;
import com.daimler.data.dto.department.DepartmentVO;
import com.daimler.data.dto.divisions.DivisionReportVO;
import com.daimler.data.dto.report.CreatedByVO;
import com.daimler.data.dto.report.CustomerVO;
import com.daimler.data.dto.report.DataSourceVO;
import com.daimler.data.dto.report.InternalCustomerVO;
import com.daimler.data.dto.report.KPIVO;
import com.daimler.data.dto.report.MemberVO;
import com.daimler.data.dto.report.ProcessOwnerCollection;
import com.daimler.data.dto.report.ReportResponseVO;
import com.daimler.data.dto.report.ReportVO;
import com.daimler.data.dto.report.SingleDataSourceVO;
import com.daimler.data.dto.report.SubdivisionVO;
import com.daimler.data.dto.report.TeamMemberVO;
import com.daimler.data.dto.solution.ChangeLogVO;
import com.daimler.data.dto.solution.UserInfoVO;
import com.daimler.data.dto.tag.TagVO;
import com.daimler.data.service.common.BaseCommonService;
import com.daimler.data.service.department.DepartmentService;
import com.daimler.data.service.kpiName.KpiNameService;
import com.daimler.data.service.tag.TagService;
import com.daimler.data.util.ConstantsUtility;
import com.daimler.dna.notifications.common.producer.KafkaProducerService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.common.collect.MapDifference.ValueDifference;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import io.jsonwebtoken.lang.Strings;

@Service
@SuppressWarnings(value = "unused")
public class BaseReportService extends BaseCommonService<ReportVO, ReportNsql, String> implements ReportService {

	private static Logger LOGGER = LoggerFactory.getLogger(BaseReportService.class);

	@Autowired
	private TagService tagService;

	@Autowired
	private DepartmentService departmentService;
	
	@Autowired
	private KpiNameService kpiNameService;

	@Autowired
	private UserStore userStore;

	@Autowired
	private KafkaProducerService kafkaProducer;

	private ReportAssembler reportAssembler;

	private ReportCustomRepository reportCustomRepository;

	private ReportRepository reportRepository;

	@Autowired
	private DnaAuthClient dnaAuthClient;

	public BaseReportService() {
		super();
	}

	@Autowired
	public BaseReportService(ReportRepository reportRepository, ReportAssembler reportAssembler,
			ReportCustomRepository reportCustomRepository) {
		super(reportRepository, reportAssembler, reportCustomRepository);
		this.reportAssembler = reportAssembler;
		this.reportCustomRepository = reportCustomRepository;
		this.reportRepository = reportRepository;
	}

	@Override
	@Transactional
	public ReportVO create(ReportVO vo) {
		updateTags(vo);
		updateDepartments(vo);
		updateDataSources(vo);
		updateKpiNames(vo);
		return super.create(vo);
	}

	private void updateKpiNames(ReportVO vo) {
		List<KPIVO> kpis = vo.getKpis();
		if(kpis != null && !kpis.isEmpty()) {
			kpis.forEach(kpi -> {
				String kpiName = kpi.getName().getKpiName();
				String kpiClassification = kpi.getName().getKpiClassification();
				if(StringUtils.hasText(kpiName)) {
					KpiNameVO existingKpiNameVO = kpiNameService.findKpiNameByName(kpiName);
					if(existingKpiNameVO != null && existingKpiNameVO.getKpiClassification() == null && StringUtils.hasText(kpiClassification)) {
						existingKpiNameVO.setKpiClassification(kpiClassification);
						kpiNameService.create(existingKpiNameVO);
					}
					if (existingKpiNameVO != null && existingKpiNameVO.getKpiName() != null)
						return;
					else {
						KpiNameVO newKpiNameVO = new KpiNameVO();
						newKpiNameVO.setKpiName(kpiName);
						newKpiNameVO.setKpiClassification(kpiClassification);
						kpiNameService.create(newKpiNameVO);
					}
				}
			});
		}
	}

	@Override
	@Transactional
	public ReportVO getById(String id) {
		if (StringUtils.hasText(id)) {
			ReportVO existingVO = super.getByUniqueliteral("reportId", id);
			if (existingVO != null && existingVO.getReportId() != null) {
				return existingVO;
			} else {
				return super.getById(id);
			}
		}
		return null;
	}

	@Override
	public List<ReportVO> getAllWithFilters(Boolean published, List<String> statuses, String userId, Boolean isAdmin,
			List<String> searchTerms, List<String> tags, int offset, int limit, String sortBy, String sortOrder,
			String division, List<String> department, List<String> processOwner, List<String> art) {
		List<ReportNsql> reportEntities = reportCustomRepository.getAllWithFiltersUsingNativeQuery(published, statuses,
				userId, isAdmin, searchTerms, tags, offset, limit, sortBy, sortOrder, division, department,
				processOwner, art);
		if (!ObjectUtils.isEmpty(reportEntities))
			return reportEntities.stream().map(n -> reportAssembler.toVo(n)).collect(Collectors.toList());
		else
			return new ArrayList<>();
	}

	@Override
	public Long getCount(Boolean published, List<String> statuses, String userId, Boolean isAdmin,
			List<String> searchTerms, List<String> tags, String division, List<String> department,
			List<String> processOwner, List<String> art) {
		return reportCustomRepository.getCountUsingNativeQuery(published, statuses, userId, isAdmin, searchTerms, tags,
				division, department, processOwner, art);
	}

	@Override
	@Transactional
	public void deleteForEachReport(String name, CATEGORY category) {
		List<ReportNsql> reports = null;
		CreatedByVO currentUser = this.userStore.getVO();
		com.daimler.data.dto.solution.TeamMemberVO modifiedBy = new com.daimler.data.dto.solution.TeamMemberVO();
		BeanUtils.copyProperties(currentUser, modifiedBy);
		modifiedBy.setShortId(currentUser.getId());
		String userId = currentUser != null ? currentUser.getId() : "dna_system";
		String userName = this.currentUserName(currentUser);
		if (StringUtils.hasText(name)) {
			reports = reportCustomRepository.getAllWithFiltersUsingNativeQuery(null, null, null, true,
					Arrays.asList(name), null, 0, 0, null, null, null, null, null, null);
		}
		if (!ObjectUtils.isEmpty(reports)) {
			reports.forEach(reportNsql -> {
				Report reportJson = reportNsql.getData();
				String reportName = reportJson.getProductName();
				List<String> teamMembers = new ArrayList<>();
				List<String> teamMembersEmails = new ArrayList<>();
				List<TeamMember> reportTeamMembers = reportJson.getMember().getReportAdmins();
				for(TeamMember member:reportTeamMembers) {
					teamMembers.add(member.getShortId());
					teamMembersEmails.add(member.getEmail());
				}
				String message = "";
				List<ChangeLogVO> changeLogs = new ArrayList<>();
				ChangeLogVO changeLog = new ChangeLogVO();
				changeLog.setChangeDate(new Date());
				changeLog.setModifiedBy(modifiedBy);
				changeLog.setNewValue(null);
				changeLog.setOldValue(name);
				if (category.equals(CATEGORY.TAG)) {
					changeLog.setChangeDescription("Tags: Tag '" + name + "' removed.");
					changeLog.setFieldChanged("/tags/");
					message = "Tag " + name + " has been deleted by Admin " + userName
							+ ". Cascading update to Report " + reportName
							+ " has been applied to remove references.";
					List<String> tags = reportNsql.getData().getDescription().getTags();
					if (!ObjectUtils.isEmpty(tags)) {
						Iterator<String> itr = tags.iterator();
						while (itr.hasNext()) {
							String tag = itr.next();
							if (tag.equals(name)) {
								itr.remove();
								break;
							}
						}
					}
				} else if (category.equals(CATEGORY.DEPARTMENT)) {
					changeLog.setChangeDescription("Departments: Department '" + name + "' removed.");
					changeLog.setFieldChanged("/departments/");
					message = "Department " + name + " has been deleted by Admin " + userName
							+ ". Cascading update to Report " + reportName
							+ " has been applied to remove references.";
					String department = reportNsql.getData().getDescription().getDepartment();
					if (StringUtils.hasText(department) && department.equals(name)) {
						reportNsql.getData().getDescription().setDepartment(null);
					}
				} else if (category.equals(CATEGORY.INTEGRATED_PORTAL)) {
					changeLog.setChangeDescription("Integrated Portals: Integrated Portal '" + name + "' removed.");
					changeLog.setFieldChanged("/integratedportals/");
					message = "Integrated Portal " + name + " has been deleted by Admin " + userName
							+ ". Cascading update to Report " + reportName
							+ " has been applied to remove references.";
					String integratedPortal = reportNsql.getData().getDescription().getIntegratedPortal();
					if (StringUtils.hasText(integratedPortal) && integratedPortal.equals(name)) {
						reportNsql.getData().getDescription().setIntegratedPortal(null);
					}
				} else if (category.equals(CATEGORY.FRONTEND_TECH)) {
					changeLog.setChangeDescription("Frontend Technologies: Frontend Technology '" + name + "' removed.");
					changeLog.setFieldChanged("/frontendtechnologies/");
					message = "Frontend Technology " + name + " has been deleted by Admin " + userName
							+ ". Cascading update to Report " + reportName
							+ " has been applied to remove references.";
					List<String> frontendTechnologies = reportNsql.getData().getDescription().getFrontendTechnologies();
					if (!ObjectUtils.isEmpty(frontendTechnologies)) {
						Iterator<String> itr = frontendTechnologies.iterator();
						while (itr.hasNext()) {
							String frontendTechnology = itr.next();
							if (frontendTechnology.equals(name)) {
								itr.remove();
								break;
							}
						}
					}
				} else if (category.equals(CATEGORY.ART)) {
					changeLog.setChangeDescription("Agile release trains: Agile release trian '" + name + "' removed.");
					changeLog.setFieldChanged("/agilereleasetrains/");
					message = "Agile release trian " + name + " has been deleted by Admin " + userName
							+ ". Cascading update to Report " + reportName
							+ " has been applied to remove references.";
					String art = reportNsql.getData().getDescription().getAgileReleaseTrain();
					if (StringUtils.hasText(art) && art.equals(name)) {
						reportNsql.getData().getDescription().setAgileReleaseTrain(null);
					}
				} else if (category.equals(CATEGORY.STATUS)) {
					changeLog.setChangeDescription("Statuses: Status '" + name + "' removed.");
					changeLog.setFieldChanged("/statuses/");
					message = "Status " + name + " has been deleted by Admin " + userName
							+ ". Cascading update to Report " + reportName
							+ " has been applied to remove references.";
					String status = reportNsql.getData().getDescription().getStatus();
					if (StringUtils.hasText(status) && status.equals(name)) {
						reportNsql.getData().getDescription().setStatus(null);
					}
				} else if (category.equals(CATEGORY.CUST_DEPARTMENT)) {
					changeLog.setChangeDescription("Customer Departments: Customer Department '" + name + "' removed.");
					changeLog.setFieldChanged("/departments/");
					message = "Customer Department " + name + " has been deleted by Admin " + userName
							+ ". Cascading update to Report " + reportName
							+ " has been applied to remove references.";
					List<InternalCustomer> customers = reportNsql.getData().getCustomer().getInternalCustomers();
					if (!ObjectUtils.isEmpty(customers)) {
						for (InternalCustomer customer : customers) {
							if (StringUtils.hasText(customer.getDepartment())
									&& customer.getDepartment().equals(name)) {
								customer.setDepartment(null);
							}
						}
					}
				} else if (category.equals(CATEGORY.LEVEL)) {
					changeLog.setChangeDescription("Levels: Level '" + name + "' removed.");
					changeLog.setFieldChanged("/levels/");
					message = "Level " + name + " has been deleted by Admin " + userName
							+ ". Cascading update to Report " + reportName
							+ " has been applied to remove references.";
					List<InternalCustomer> customers = reportNsql.getData().getCustomer().getInternalCustomers();
					if (!ObjectUtils.isEmpty(customers)) {
						for (InternalCustomer customer : customers) {
							if (StringUtils.hasText(customer.getLevel()) && customer.getLevel().equals(name)) {
								customer.setLevel(null);
							}
						}
					}
				} else if (category.equals(CATEGORY.LEGAL_ENTITY)) {
					changeLog.setChangeDescription("Legal entities: Legal entity '" + name + "' removed.");
					changeLog.setFieldChanged("/legalentities/");
					message = "Legal entity " + name + " has been deleted by Admin " + userName
							+ ". Cascading update to Report " + reportName
							+ " has been applied to remove references.";
					List<InternalCustomer> customers = reportNsql.getData().getCustomer().getInternalCustomers();
					if (!ObjectUtils.isEmpty(customers)) {
						for (InternalCustomer customer : customers) {
							if (StringUtils.hasText(customer.getLegalEntity())
									&& customer.getLegalEntity().equals(name)) {
								customer.setLegalEntity(null);
							}
						}
					}
				} else if (category.equals(CATEGORY.KPI_CLASSIFICATION)) {
					changeLog.setChangeDescription("kpi Classifications: kpi Classification '" + name + "' removed.");
					changeLog.setFieldChanged("/kpiClassifications/");
					message = "Kpi Classification " + name + " has been deleted by Admin " + userName
							+ ". Cascading update to Report " + reportName
							+ " has been applied to remove references.";
					List<KPI> kpis = reportNsql.getData().getKpis();
					if (!ObjectUtils.isEmpty(kpis)) {
						for (KPI kpi : kpis) {
							KPIName kpiNameObject = kpi.getName();
							if (StringUtils.hasText(kpiNameObject.getKpiClassification()) && kpiNameObject.getKpiClassification().equals(name)) {
								kpiNameObject.setKpiClassification(null);
							}
							kpi.setName(kpiNameObject);
						}
					}
				}else if (category.equals(CATEGORY.KPI_NAME)) {
					changeLog.setChangeDescription("kpi Names: kpi Name '" + name + "' removed.");
					changeLog.setFieldChanged("/kpiNames/");
					message = "Kpi Name " + name + " has been deleted by Admin " + userName
							+ ". Cascading update to Report " + reportName
							+ " has been applied to remove references.";
					List<KPI> kpis = reportNsql.getData().getKpis();
					if (!ObjectUtils.isEmpty(kpis)) {
						for (KPI kpi : kpis) {
							KPIName kpiNameObject = kpi.getName();
							if (StringUtils.hasText(kpiNameObject.getKpiName()) && kpiNameObject.getKpiName().equals(name)) {
								kpiNameObject.setKpiName(null);
							}
							kpi.setName(kpiNameObject);
						}
					}
				} else if (category.equals(CATEGORY.REPORTING_CAUSE)) {
					changeLog.setChangeDescription("Reporting causes: Reporting cause '" + name + "' removed.");
					changeLog.setFieldChanged("/reportingcauses/");
					message = "Reporting cause " + name + " has been deleted by Admin " + userName
							+ ". Cascading update to Report " + reportName
							+ " has been applied to remove references.";
					List<KPI> kpis = reportNsql.getData().getKpis();
					if (!ObjectUtils.isEmpty(kpis)) {
						for (KPI kpi : kpis) {
							List<String> reportingCauses = kpi.getReportingCause();
							List<String> newReportingCauses = new ArrayList<>();
							if(reportingCauses != null) {
							for(String reportingCause : reportingCauses) {
								if (StringUtils.hasText(reportingCause) && reportingCause.equals(name)) {
									kpi.setReportingCause(null);
								}
								else {
									newReportingCauses.add(reportingCause);									
								}
							}
							kpi.setReportingCause(newReportingCauses);
						   }
						}
					}		
				} else if (category.equals(CATEGORY.DATASOURCE)) {
					changeLog.setChangeDescription("Datasources: Datasource '" + name + "' removed.");
					changeLog.setFieldChanged("/dataSources/");
					message = "Datasource " + name + " has been deleted by Admin " + userName
							+ ". Cascading update to report " + reportName
							+ " has been applied to remove references.";
					List<SingleDataSource> singleDataSources = reportNsql.getData().getSingleDataSources();
					if (!ObjectUtils.isEmpty(singleDataSources)) {
						for (SingleDataSource singleDataSource : singleDataSources) {
							List<DataSource> dataSources = singleDataSource.getDataSources();
							if (!ObjectUtils.isEmpty(dataSources)) {
								Iterator<DataSource> itr = dataSources.iterator();
								while (itr.hasNext()) {
									DataSource dataSource = itr.next();
									if (dataSource.getDataSource().equals(name)) {
										itr.remove();
									}
								}
							}
						}
					}

				} else if (category.equals(CATEGORY.CONNECTION_TYPE)) {
					changeLog.setChangeDescription("Connection types: Connection type '" + name + "' removed.");
					changeLog.setFieldChanged("/connectiontypes/");
					message = "Connection types " + name + " has been deleted by Admin " + userName
							+ ". Cascading update to Report " + reportName
							+ " has been applied to remove references.";
					List<SingleDataSource> singleDataSources = reportNsql.getData().getSingleDataSources();
					if (!ObjectUtils.isEmpty(singleDataSources)) {
						for (SingleDataSource singleDataSource : singleDataSources) {
							if (StringUtils.hasText(singleDataSource.getConnectionType())
									&& singleDataSource.getConnectionType().equals(name)) {
								singleDataSource.setConnectionType(null);
							}
						}
					}
					List<DataWarehouse> dataWarehouses = reportNsql.getData().getDataWarehouses();
					if (!ObjectUtils.isEmpty(dataWarehouses)) {
						for (DataWarehouse dataWarehouse : dataWarehouses) {
							if (StringUtils.hasText(dataWarehouse.getConnectionType())
									&& dataWarehouse.getConnectionType().equals(name)) {
								dataWarehouse.setConnectionType(null);
							}
						}
					}
				} else if (category.equals(CATEGORY.DATA_CLASSIFICATION)) {
					changeLog.setChangeDescription("Data classifications: Data classification '" + name + "' removed.");
					changeLog.setFieldChanged("/dataclassifications/");
					message = "Data classification " + name + " has been deleted by Admin " + userName
							+ ". Cascading update to Report " + reportName
							+ " has been applied to remove references.";
					List<SingleDataSource> singleDataSources = reportNsql.getData().getSingleDataSources();
					if (!ObjectUtils.isEmpty(singleDataSources)) {
						for (SingleDataSource singleDataSource : singleDataSources) {
							if (StringUtils.hasText(singleDataSource.getDataClassification())
									&& singleDataSource.getDataClassification().equals(name)) {
								singleDataSource.setDataClassification(null);
							}
						}
					}
					List<DataWarehouse> dataWarehouses = reportNsql.getData().getDataWarehouses();
					if (!ObjectUtils.isEmpty(dataWarehouses)) {
						for (DataWarehouse dataWarehouse : dataWarehouses) {
							if (StringUtils.hasText(dataWarehouse.getDataClassification())
									&& dataWarehouse.getDataClassification().equals(name)) {
								dataWarehouse.setDataClassification(null);
							}
						}
					}
				} else if (category.equals(CATEGORY.DATA_WAREHOUSE)) {
					changeLog.setChangeDescription("Datawarehouses: Datawarehouse '" + name + "' removed.");
					changeLog.setFieldChanged("/datawarehouses/");
					message = "Datawarehouse " + name + " has been deleted by Admin " + userName
							+ ". Cascading update to Report " + reportName
							+ " has been applied to remove references.";
					List<DataWarehouse> dataWarehouses = reportNsql.getData().getDataWarehouses();
					if (!ObjectUtils.isEmpty(dataWarehouses)) {
						for (DataWarehouse dataWarehouse : dataWarehouses) {
							if (StringUtils.hasText(dataWarehouse.getDataWarehouse())
									&& dataWarehouse.getDataWarehouse().equals(name)) {
								dataWarehouse.setDataWarehouse(null);
							}
						}
					}
				} else if (category.equals(CATEGORY.DIVISION)) {
					changeLog.setChangeDescription("Divisions: Division '" + name + "' removed.");
					changeLog.setFieldChanged("/divisions/");
					message = "Division " + name + " has been deleted by Admin " + userName
							+ ". Cascading update to Report " + reportName
							+ " has been applied to remove references.";
					Division reportdivision = reportNsql.getData().getDescription().getDivision();
					if (Objects.nonNull(reportdivision) && StringUtils.hasText(reportdivision.getId())
							&& reportdivision.getId().equals(name)) {
						reportdivision.setName(null);
						reportdivision.setId(null);
						reportdivision.setSubdivision(null);
					}
				}
				reportCustomRepository.update(reportNsql);
				changeLogs.add(changeLog);
				LOGGER.info(
						"Publishing message on Dashboard-Report MDM Delete event for report {}, after admin action on {} and {}",
						reportName, category, name);
				kafkaProducer.send("Dashboard-Report Updated after Admin action", reportNsql.getId(), "", userId, message,
						true, teamMembers, teamMembersEmails, changeLogs);				

			});
		}
	}

	@Override
	@Transactional
	public void updateForEachReport(String oldValue, String newValue, CATEGORY category, Object updateObject) {
		List<ReportNsql> reports = null;
		CreatedByVO currentUser = this.userStore.getVO();
		com.daimler.data.dto.solution.TeamMemberVO modifiedBy = new com.daimler.data.dto.solution.TeamMemberVO();
		BeanUtils.copyProperties(currentUser, modifiedBy);
		modifiedBy.setShortId(currentUser.getId());
		String userId = currentUser != null ? currentUser.getId() : "dna_system";
		String userName = this.currentUserName(currentUser);
		if (StringUtils.hasText(oldValue)) {
			reports = reportCustomRepository.getAllWithFiltersUsingNativeQuery(null, null, null, true,
					Arrays.asList(oldValue), null, 0, 0, null, null, null, null, null, null);
		}
		if (!ObjectUtils.isEmpty(reports)) {
			reports.forEach(reportNsql -> {
				Report reportJson = reportNsql.getData();
				String reportName = reportJson.getProductName();
				List<String> teamMembers = new ArrayList<>();
				List<String> teamMembersEmails = new ArrayList<>();
				List<TeamMember> reportTeamMembers = reportJson.getMember().getReportAdmins();
				for(TeamMember member:reportTeamMembers) {
					teamMembers.add(member.getShortId());
					teamMembersEmails.add(member.getEmail());
				}
				String message = "";
				List<ChangeLogVO> changeLogs = new ArrayList<>();
				ChangeLogVO changeLog = new ChangeLogVO();
				changeLog.setChangeDate(new Date());
				changeLog.setModifiedBy(modifiedBy);
				changeLog.setNewValue(null);
				changeLog.setOldValue(oldValue);
				if (category.equals(CATEGORY.TAG)) {
					changeLog.setChangeDescription("Tags: Tag '" + oldValue + "' updated.");
					changeLog.setFieldChanged("/tags/");
					message = "Tag " + oldValue + " has been updated by Admin " + userName
							+ ". Cascading update to Report " + reportName
							+ " has been applied to update references.";
					List<String> tags = reportNsql.getData().getDescription().getTags();
					if (!ObjectUtils.isEmpty(tags)) {
						ListIterator<String> itr = tags.listIterator();
						while (itr.hasNext()) {
							String tag = itr.next();
							if (tag.equals(oldValue)) {
								itr.set(newValue);
								break;
							}
						}
					}
				} else if (category.equals(CATEGORY.DEPARTMENT)) {
					changeLog.setChangeDescription("Departments: Department '" + oldValue + "' updated.");
					changeLog.setFieldChanged("/departments/");
					message = "Department " + oldValue + " has been updated by Admin " + userName
							+ ". Cascading update to Report " + reportName
							+ " has been applied to update references.";
					String department = reportNsql.getData().getDescription().getDepartment();
					if (StringUtils.hasText(department) && department.equals(oldValue)) {
						reportNsql.getData().getDescription().setDepartment(newValue);
					}
				} else if (category.equals(CATEGORY.INTEGRATED_PORTAL)) {
					changeLog.setChangeDescription("Integrated Portals: Integrated Portal '" + oldValue + "' updated.");
					changeLog.setFieldChanged("/integratedportals/");
					message = "Integrated Portal " + oldValue + " has been updated by Admin " + userName
							+ ". Cascading update to Report " + reportName
							+ " has been applied to update references.";
					String integratedPortal = reportNsql.getData().getDescription().getIntegratedPortal();
					if (StringUtils.hasText(integratedPortal) && integratedPortal.equals(oldValue)) {
						reportNsql.getData().getDescription().setIntegratedPortal(newValue);
					}
				} else if (category.equals(CATEGORY.FRONTEND_TECH)) {
					changeLog.setChangeDescription("Frontend Technologies: Frontend Technology '" + oldValue + "' updated.");
					changeLog.setFieldChanged("/frontendtechnologies/");
					message = "Frontend Technology " + oldValue + " has been updated by Admin " + userName
							+ ". Cascading update to Report " + reportName
							+ " has been applied to update references.";
					List<String> frontendTechnologies = reportNsql.getData().getDescription().getFrontendTechnologies();
					if (!ObjectUtils.isEmpty(frontendTechnologies)) {
						ListIterator<String> itr = frontendTechnologies.listIterator();
						while (itr.hasNext()) {
							String frontendTechnology = itr.next();
							if (frontendTechnology.equals(oldValue)) {
								itr.set(newValue);
								break;
							}
						}
					}
				} else if (category.equals(CATEGORY.ART)) {
					changeLog.setChangeDescription("Agile release trains: Agile release trian '" + oldValue + "' updated.");
					changeLog.setFieldChanged("/agilereleasetrains/");
					message = "Agile release trian " + oldValue + " has been updated by Admin " + userName
							+ ". Cascading update to Report " + reportName
							+ " has been applied to update references.";
					String art = reportNsql.getData().getDescription().getAgileReleaseTrain();
					if (StringUtils.hasText(art) && art.equals(oldValue)) {
						reportNsql.getData().getDescription().setAgileReleaseTrain(newValue);
					}
				} else if (category.equals(CATEGORY.STATUS)) {
					changeLog.setChangeDescription("Statuses: Status '" + oldValue + "' updated.");
					changeLog.setFieldChanged("/statuses/");
					message = "Status " + oldValue + " has been updated by Admin " + userName
							+ ". Cascading update to Report " + reportName
							+ " has been applied to updated references.";
					String status = reportNsql.getData().getDescription().getStatus();
					if (StringUtils.hasText(status) && status.equals(oldValue)) {
						reportNsql.getData().getDescription().setStatus(newValue);
					}
				} else if (category.equals(CATEGORY.CUST_DEPARTMENT)) {
					changeLog.setChangeDescription("Customer Departments: Customer Department '" + oldValue + "' updated.");
					changeLog.setFieldChanged("/departments/");
					message = "Customer Department " + oldValue + " has been updated by Admin " + userName
							+ ". Cascading update to Report " + reportName
							+ " has been applied to update references.";
					List<InternalCustomer> customers = reportNsql.getData().getCustomer().getInternalCustomers();
					if (!ObjectUtils.isEmpty(customers)) {
						for (InternalCustomer customer : customers) {
							if (StringUtils.hasText(customer.getDepartment())
									&& customer.getDepartment().equals(oldValue)) {
								customer.setDepartment(newValue);
							}
						}
					}
				} else if (category.equals(CATEGORY.LEVEL)) {
					changeLog.setChangeDescription("Levels: Level '" + oldValue + "' updated.");
					changeLog.setFieldChanged("/levels/");
					message = "Level " + oldValue + " has been updated by Admin " + userName
							+ ". Cascading update to Report " + reportName
							+ " has been applied to update references.";
					List<InternalCustomer> customers = reportNsql.getData().getCustomer().getInternalCustomers();
					if (!ObjectUtils.isEmpty(customers)) {
						for (InternalCustomer customer : customers) {
							if (StringUtils.hasText(customer.getLevel()) && customer.getLevel().equals(oldValue)) {
								customer.setLevel(newValue);
							}
						}
					}
				} else if (category.equals(CATEGORY.LEGAL_ENTITY)) {
					changeLog.setChangeDescription("Legal entities: Legal entity '" + oldValue + "' updated.");
					changeLog.setFieldChanged("/legalentities/");
					message = "Legal entity " + oldValue + " has been updated by Admin " + userName
							+ ". Cascading update to Report " + reportName
							+ " has been applied to update references.";
					List<InternalCustomer> customers = reportNsql.getData().getCustomer().getInternalCustomers();
					if (!ObjectUtils.isEmpty(customers)) {
						for (InternalCustomer customer : customers) {
							if (StringUtils.hasText(customer.getLegalEntity())
									&& customer.getLegalEntity().equals(oldValue)) {
								customer.setLegalEntity(newValue);
							}
						}
					}
				} else if (category.equals(CATEGORY.KPI_CLASSIFICATION)) {
					changeLog.setChangeDescription("kpi Classifications: kpi Classification '" + oldValue + "' updated.");
					changeLog.setFieldChanged("/kpiClassifications/");
					message = "Kpi Classification " + oldValue + " has been updated by Admin " + userName
							+ ". Cascading update to Report " + reportName
							+ " has been applied to update references.";
					List<KPI> kpis = reportNsql.getData().getKpis();
					if (!ObjectUtils.isEmpty(kpis)) {
						for (KPI kpi : kpis) {
							KPIName kpiNameObject = kpi.getName();
							if(StringUtils.hasText(kpiNameObject.getKpiClassification()) && kpiNameObject.getKpiClassification().equals(oldValue)) {
								kpiNameObject.setKpiClassification(newValue);
							}
							kpi.setName(kpiNameObject);						
						}
					}
				}else if (category.equals(CATEGORY.KPI_NAME)) {
					changeLog.setChangeDescription("kpi Names: kpi Name '" + oldValue + "' updated.");
					changeLog.setFieldChanged("/kpiNames/");
					message = "Kpi Name " + oldValue + " has been updated by Admin " + userName
							+ ". Cascading update to Report " + reportName
							+ " has been applied to update references.";
					List<KPI> kpis = reportNsql.getData().getKpis();
					if (!ObjectUtils.isEmpty(kpis)) {
						for (KPI kpi : kpis) {
							KPIName kpiNameObject = kpi.getName();
							if(StringUtils.hasText(kpiNameObject.getKpiName()) && kpiNameObject.getKpiName().equals(oldValue)) {
								kpiNameObject.setKpiName(newValue);
							}
							kpi.setName(kpiNameObject);						
						}
					}
				} else if (category.equals(CATEGORY.REPORTING_CAUSE)) {
					changeLog.setChangeDescription("Reporting causes: Reporting cause '" + oldValue + "' updated.");
					changeLog.setFieldChanged("/reportingcauses/");
					message = "Reporting cause " + oldValue + " has been updated by Admin " + userName
							+ ". Cascading update to Report " + reportName
							+ " has been applied to update references.";
					List<KPI> kpis = reportNsql.getData().getKpis();
					if (!ObjectUtils.isEmpty(kpis)) {
						for (KPI kpi : kpis) {
							List<String> reportingCauses = kpi.getReportingCause();
							List<String> newReportingCauses = new ArrayList<>();
							if(reportingCauses != null) {
							for(String reportingCause : reportingCauses) {
								if (StringUtils.hasText(reportingCause) && reportingCause.equals(oldValue)) {
									newReportingCauses.add(newValue);									
								}
								else {
									newReportingCauses.add(reportingCause);
								}
							}
							kpi.setReportingCause(newReportingCauses);
						   }
						}
					}
				} else if (category.equals(CATEGORY.CONNECTION_TYPE)) {
					changeLog.setChangeDescription("Connection types: Connection type '" + oldValue + "' updated.");
					changeLog.setFieldChanged("/connectiontypes/");
					message = "Connection types " + oldValue + " has been updated by Admin " + userName
							+ ". Cascading update to Report " + reportName
							+ " has been applied to update references.";
					List<SingleDataSource> singleDataSources = reportNsql.getData().getSingleDataSources();
					if (!ObjectUtils.isEmpty(singleDataSources)) {
						for (SingleDataSource singleDataSource : singleDataSources) {
							if (StringUtils.hasText(singleDataSource.getConnectionType())
									&& singleDataSource.getConnectionType().equals(oldValue)) {
								singleDataSource.setConnectionType(newValue);
							}
						}
					}

					List<DataWarehouse> dataWarehouses = reportNsql.getData().getDataWarehouses();
					if (!ObjectUtils.isEmpty(dataWarehouses)) {
						for (DataWarehouse dataWarehouse : dataWarehouses) {
							if (StringUtils.hasText(dataWarehouse.getConnectionType())
									&& dataWarehouse.getConnectionType().equals(oldValue)) {
								dataWarehouse.setConnectionType(newValue);
							}
						}
					}
				} else if (category.equals(CATEGORY.DATA_CLASSIFICATION)) {
					changeLog.setChangeDescription("Data classifications: Data classification '" + oldValue + "' updated.");
					changeLog.setFieldChanged("/dataclassifications/");
					message = "Data classification " + oldValue + " has been updated by Admin " + userName
							+ ". Cascading update to Report " + reportName
							+ " has been applied to update references.";
					List<SingleDataSource> singleDataSources = reportNsql.getData().getSingleDataSources();
					if (!ObjectUtils.isEmpty(singleDataSources)) {
						for (SingleDataSource singleDataSource : singleDataSources) {
							if (StringUtils.hasText(singleDataSource.getDataClassification())
									&& singleDataSource.getDataClassification().equals(oldValue)) {
								singleDataSource.setDataClassification(newValue);
							}
						}
					}

					List<DataWarehouse> dataWarehouses = reportNsql.getData().getDataWarehouses();
					if (!ObjectUtils.isEmpty(dataWarehouses)) {
						for (DataWarehouse dataWarehouse : dataWarehouses) {
							if (StringUtils.hasText(dataWarehouse.getDataClassification())
									&& dataWarehouse.getDataClassification().equals(oldValue)) {
								dataWarehouse.setDataClassification(newValue);
							}
						}
					}
				} else if (category.equals(CATEGORY.DATA_WAREHOUSE)) {
					changeLog.setChangeDescription("Datawarehouses: Datawarehouse '" + oldValue + "' updated.");
					changeLog.setFieldChanged("/datawarehouses/");
					message = "Datawarehouse " + oldValue + " has been updated by Admin " + userName
							+ ". Cascading update to Report " + reportName
							+ " has been applied to update references.";
					List<DataWarehouse> dataWarehouses = reportNsql.getData().getDataWarehouses();
					if (!ObjectUtils.isEmpty(dataWarehouses)) {
						for (DataWarehouse dataWarehouse : dataWarehouses) {
							if (StringUtils.hasText(dataWarehouse.getDataWarehouse())
									&& dataWarehouse.getDataWarehouse().equals(oldValue)) {
								dataWarehouse.setDataWarehouse(newValue);
							}
						}
					}
				} else if (category.equals(CATEGORY.DIVISION)) {
					changeLog.setChangeDescription("Divisions: Division '" + oldValue + "' updated.");
					changeLog.setFieldChanged("/divisions/");
					message = "Division " + oldValue + " has been updated by Admin " + userName
							+ ". Cascading update to Report " + reportName
							+ " has been applied to update references.";
					Division reportdivision = reportNsql.getData().getDescription().getDivision();
					DivisionReportVO divisionVO = (DivisionReportVO) updateObject;
					if (Objects.nonNull(reportdivision) && StringUtils.hasText(reportdivision.getId())
							&& reportdivision.getId().equals(divisionVO.getId())) {
						reportdivision.setName(divisionVO.getName().toUpperCase());
						Subdivision subdivision = reportdivision.getSubdivision();
						List<SubdivisionVO> subdivisionlist = divisionVO.getSubdivisions();
						if (Objects.nonNull(subdivision)) {
							if (ObjectUtils.isEmpty(subdivisionlist)) {
								reportdivision.setSubdivision(null);
							} else {
								boolean exists = false;
								for (SubdivisionVO value : subdivisionlist) {
									if (StringUtils.hasText(value.getId())
											&& value.getId().equals(subdivision.getId())) {
										Subdivision subdiv = new Subdivision();
										subdiv.setId(value.getId());
										subdiv.setName(value.getName().toUpperCase());
										reportdivision.setSubdivision(subdiv);
										exists = true;
										break;
									}
								}
								if (!exists) {
									reportdivision.setSubdivision(null);
								}
							}
						}
					}
				}
				reportCustomRepository.update(reportNsql);
				changeLogs.add(changeLog);
				LOGGER.info(
						"Publishing message on Dashboard-Report MDM Update event for report {}, after admin action on {} and {}",
						reportName, category, oldValue);
				kafkaProducer.send("Dashboard-Report Updated after Admin action", reportNsql.getId(), "", userId, message,
						true, teamMembers, teamMembersEmails, changeLogs);				
			});
		}
	}

	private void updateTags(ReportVO vo) {
		List<String> tags = vo.getDescription().getTags();
		if (tags != null && !tags.isEmpty()) {
			tags.forEach(tag -> {
				TagVO existingTagVO = tagService.findTagByName(tag);
				if (existingTagVO != null && existingTagVO.getName() != null)
					return;
				else {
					TagVO newTagVO = new TagVO();
					newTagVO.setName(tag);
					tagService.create(newTagVO);
				}
			});
		}
	}

	private void updateDepartments(ReportVO vo) {
		String department = vo.getDescription().getDepartment();
		if (Strings.hasText(department)) {
			DepartmentVO existingDepartmentVO = departmentService.findDepartmentByName(department);
			if (existingDepartmentVO != null && existingDepartmentVO.getName() != null)
				return;
			else {
				DepartmentVO newDepartmentVO = new DepartmentVO();
				newDepartmentVO.setName(department);
				departmentService.create(newDepartmentVO);
			}

		}
	}

	private void updateDataSources(ReportVO vo) {
		List<DataSourceCreateVO> dataSourcesCreateVO = new ArrayList<>();
		if (vo.getDataAndFunctions() != null && !ObjectUtils.isEmpty(vo.getDataAndFunctions().getSingleDataSources())) {
			List<SingleDataSourceVO> singleDataSources = vo.getDataAndFunctions().getSingleDataSources();
			for (SingleDataSourceVO singleDataSource : singleDataSources) {
				List<DataSourceVO> dataSources = singleDataSource.getDataSources();
				for (DataSourceVO dataSource : dataSources) {
					DataSourceCreateVO newDataSourceVO = new DataSourceCreateVO();
					newDataSourceVO.setName(dataSource.getDataSource());
					dataSourcesCreateVO.add(newDataSourceVO);
				}
			}
			DataSourceBulkRequestVO requestVO = new DataSourceBulkRequestVO();
			requestVO.setData(dataSourcesCreateVO);
			dnaAuthClient.createDataSources(requestVO);
		}
	}

	@Override
	@Transactional
	public ResponseEntity<ReportResponseVO> createReport(ReportVO requestReportVO) {
		ReportResponseVO reportResponseVO = new ReportResponseVO();
		try {
			String uniqueProductName = requestReportVO.getProductName();
			ReportVO existingReportVO = super.getByUniqueliteral("productName", uniqueProductName);
			if (existingReportVO != null && existingReportVO.getProductName() != null) {
				reportResponseVO.setData(existingReportVO);
				List<MessageDescription> messages = new ArrayList<>();
				MessageDescription message = new MessageDescription();
				message.setMessage("Report already exists.");
				messages.add(message);
				reportResponseVO.setErrors(messages);
				LOGGER.info("Report {} already exists, returning as CONFLICT", uniqueProductName);
				return new ResponseEntity<>(reportResponseVO, HttpStatus.CONFLICT);
			}
			requestReportVO.setCreatedBy(this.userStore.getVO());
			requestReportVO.setCreatedDate(new Date());
			requestReportVO.setId(null);
			requestReportVO.setReportId("REP-" + String.format("%05d", reportRepository.getNextSeqId()));
			if (requestReportVO.isPublish() == null)
				requestReportVO.setPublish(false);

			ReportVO reportVO = this.create(requestReportVO);
			if (reportVO != null && reportVO.getId() != null) {
				String eventType = "Dashboard-Report Create";
				this.publishEventMessages(eventType, reportVO);
				reportResponseVO.setData(reportVO);
				LOGGER.info("Report {} created successfully", uniqueProductName);
				return new ResponseEntity<>(reportResponseVO, HttpStatus.CREATED);
			} else {
				List<MessageDescription> messages = new ArrayList<>();
				MessageDescription message = new MessageDescription();
				message.setMessage("Failed to save due to internal error");
				messages.add(message);
				reportResponseVO.setData(requestReportVO);
				reportResponseVO.setErrors(messages);
				LOGGER.error("Report {} , failed to create", uniqueProductName);
				return new ResponseEntity<>(reportResponseVO, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (Exception e) {
			LOGGER.error("Exception occurred:{} while creating report {} ", e.getMessage(),
					requestReportVO.getProductName());
			List<MessageDescription> messages = new ArrayList<>();
			MessageDescription message = new MessageDescription();
			message.setMessage(e.getMessage());
			messages.add(message);
			reportResponseVO.setData(requestReportVO);
			reportResponseVO.setErrors(messages);
			return new ResponseEntity<>(reportResponseVO, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	@Transactional
	public ResponseEntity<ReportResponseVO> updateReport(ReportVO requestReportVO) {
		ReportResponseVO response = new ReportResponseVO();
		try {
			String id = requestReportVO.getId();
			ReportVO existingReportVO = super.getById(id);
			ReportVO mergedReportVO = null;
			if (requestReportVO.isPublish() == null) {
				requestReportVO.setPublish(false);
			}
			if (existingReportVO != null && existingReportVO.getId() != null) {
				CreatedByVO createdBy = existingReportVO.getCreatedBy();
				if (canProceedToEdit(existingReportVO)) {
					requestReportVO.setCreatedBy(createdBy);
					requestReportVO.setCreatedDate(existingReportVO.getCreatedDate());
					requestReportVO.lastModifiedDate(new Date());
					requestReportVO.setReportId(existingReportVO.getReportId());
					mergedReportVO = this.create(requestReportVO);
					if (mergedReportVO != null && mergedReportVO.getId() != null) {
						response.setData(mergedReportVO);
						response.setErrors(null);
						LOGGER.info("Report with id {} updated successfully", id);
						try {
							if (mergedReportVO.isPublish()) {
								CreatedByVO modifyingUser = this.userStore.getVO();
								String eventType = "Dashboard-Report Update";
								String resourceID = mergedReportVO.getId();
								String reportName = mergedReportVO.getProductName();
								String publishingUserId = "dna_system";
								String publishingUserName = "";
								if (modifyingUser != null) {
									publishingUserId = modifyingUser.getId();
									publishingUserName = modifyingUser.getFirstName() + " "
											+ modifyingUser.getLastName();
									if (publishingUserName == null || "".equalsIgnoreCase(publishingUserName))
										publishingUserName = publishingUserId;
								}
								List<String> membersId = new ArrayList<>();
								List<String> membersEmail = new ArrayList<>();
								MemberVO memberVO = mergedReportVO.getMembers();

								List<TeamMemberVO> members = new ArrayList<>();
								if (memberVO.getReportAdmins() != null) {
									members.addAll(memberVO.getReportAdmins());
								}								

								CustomerVO customerVO = mergedReportVO.getCustomer();
								if (customerVO != null && !ObjectUtils.isEmpty(customerVO.getInternalCustomers())) {
									for (InternalCustomerVO internalCustomerVO : customerVO.getInternalCustomers()) {
										if (internalCustomerVO.getProcessOwner() != null) {
											members.add(internalCustomerVO.getProcessOwner());
										}
									}
								}
								for (TeamMemberVO member : members) {
									if (member != null) {
										String memberId = member.getShortId() != null ? member.getShortId() : "";
										if (!membersId.contains(memberId)) {
											membersId.add(memberId);
											String emailId = member.getEmail() != null ? member.getEmail() : "";
											membersEmail.add(emailId);
										}
									}
								}
								String eventMessage = "Dashboard report " + reportName + " has been updated by "
										+ publishingUserName;
								List<ChangeLogVO> changeLogs = new ArrayList<>();
								List<ChangeLogVO> newChangeLogs = new ArrayList<>();
								CreatedByVO currentUser = this.userStore.getVO();
								changeLogs = jsonObjectCompare(requestReportVO, existingReportVO, currentUser);
								if(changeLogs.size() == 1) {
									for(ChangeLogVO changeLogVO : changeLogs) {
										if(changeLogVO.getFieldChanged().contains("/lastModifiedDate")) {
											newChangeLogs.add(changeLogVO);
										}
									}
								}								
								changeLogs.removeAll(newChangeLogs);
								if(changeLogs != null && changeLogs.size()>0) {
								kafkaProducer.send(eventType, resourceID, "", publishingUserId, eventMessage, true,
										membersId, membersEmail, changeLogs);								
								LOGGER.info("Published successfully event {} for report {} with message {}", eventType,
										resourceID, eventMessage);
								}
								else {
									LOGGER.info("Changelogs are empty: {} " , changeLogs);
								}
							}
						} catch (Exception e) {
							LOGGER.error("Failed while publishing dashboard report update event msg. Exception is {} ",
									e.getMessage());
						}

						return new ResponseEntity<>(response, HttpStatus.OK);
					} else {
						List<MessageDescription> messages = new ArrayList<>();
						MessageDescription message = new MessageDescription();
						message.setMessage("Failed to update due to internal error");
						messages.add(message);
						response.setData(requestReportVO);
						response.setErrors(messages);
						LOGGER.info("Report with id {} cannot be edited. Failed with unknown internal error", id);
						return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
					}
				} else {
					List<MessageDescription> notAuthorizedMsgs = new ArrayList<>();
					MessageDescription notAuthorizedMsg = new MessageDescription();
					notAuthorizedMsg.setMessage(
							"Not authorized to edit Report. Only user who created the Report or with admin role can edit.");
					notAuthorizedMsgs.add(notAuthorizedMsg);
					response.setErrors(notAuthorizedMsgs);
					LOGGER.info("Report with id {} cannot be edited. User not authorized", id);
					return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
				}
			} else {
				List<MessageDescription> notFoundmessages = new ArrayList<>();
				MessageDescription notFoundmessage = new MessageDescription();
				notFoundmessage.setMessage("No Report found for given id. Update cannot happen");
				notFoundmessages.add(notFoundmessage);
				response.setErrors(notFoundmessages);
				LOGGER.info("No Report found for given id {} , update cannot happen.", id);
				return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			LOGGER.error("Report with id {} cannot be edited. Failed due to internal error {} ",
					requestReportVO.getId(), e.getMessage());
			List<MessageDescription> messages = new ArrayList<>();
			MessageDescription message = new MessageDescription();
			message.setMessage("Failed to update due to internal error. " + e.getMessage());
			messages.add(message);
			response.setData(requestReportVO);
			response.setErrors(messages);
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	/*
	 * To check if user has access to proceed with edit or delete of the record.
	 * 
	 */
	private boolean canProceedToEdit(ReportVO existingReportVO) {
		boolean canProceed = false;
		boolean hasAdminAccess = this.userStore.getUserInfo().hasAdminAccess();
		// To fetch user info from dna-backend by id
		UserInfoVO userInfoVO = dnaAuthClient.userInfoById(this.userStore.getUserInfo().getId());
		// To check if user having DivisionAdmin role and division is same as Report
		boolean isDivisionAdmin = userInfoVO.getRoles().stream()
				.anyMatch(n -> ConstantsUtility.DIVISION_ADMIN.equals(n.getName()))
				&& userInfoVO.getDivisionAdmins().contains(existingReportVO.getDescription().getDivision().getName());
		if (hasAdminAccess) {
			canProceed = true;
		} else if (isDivisionAdmin) {
			canProceed = true;
		} else {
			CreatedByVO currentUser = this.userStore.getVO();
			String userId = currentUser != null ? currentUser.getId() : "";
			boolean isTeamMember = false;
			if (StringUtils.hasText(userId)) {
				// To check if user is report admin(Team member)
				isTeamMember = (existingReportVO.getMembers() != null
						&& !ObjectUtils.isEmpty(existingReportVO.getMembers().getReportAdmins()))
								? existingReportVO.getMembers().getReportAdmins().stream().anyMatch(
										n -> userId.equalsIgnoreCase(n.getShortId()))
								: false;
			}
			if (isTeamMember) {
				canProceed = true;
			}
		}

		return canProceed;
	}

	@Override
	@Transactional
	public ResponseEntity<GenericMessage> deleteReport(String id) {
		try {
			ReportVO report = super.getById(id);
			if (canProceedToEdit(report)) {
				String eventType = "Dashboard-Report Delete";
				this.publishEventMessages(eventType, report);				
				this.deleteById(id);
				GenericMessage successMsg = new GenericMessage();
				successMsg.setSuccess("success");
				LOGGER.info("Report with id {} deleted successfully", id);
				return new ResponseEntity<>(successMsg, HttpStatus.OK);
			} else {
				MessageDescription notAuthorizedMsg = new MessageDescription();
				notAuthorizedMsg.setMessage(
						"Not authorized to delete Report. Only the Report owner or an admin can delete the Report.");
				GenericMessage errorMessage = new GenericMessage();
				errorMessage.addErrors(notAuthorizedMsg);
				LOGGER.debug("Report with id {} cannot be deleted. User not authorized", id);
				return new ResponseEntity<>(errorMessage, HttpStatus.FORBIDDEN);
			}
		} catch (EntityNotFoundException e) {
			MessageDescription invalidMsg = new MessageDescription("No Report with the given id");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(invalidMsg);
			LOGGER.error("No Report with the given id {} , could not delete.", id);
			return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
		} catch (Exception e) {
			MessageDescription exceptionMsg = new MessageDescription("Failed to delete due to internal error.");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(exceptionMsg);
			LOGGER.error("Failed to delete report with id {} , due to internal error.", id);
			return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public ResponseEntity<ProcessOwnerCollection> getProcessOwners() {
		ProcessOwnerCollection processOwnerCollection = new ProcessOwnerCollection();
		try {
			List<TeamMemberVO> processOwnerList = reportCustomRepository.getAllProcessOwnerUsingNativeQuery();
			LOGGER.debug("ProcessOwners fetched successfully");
			if (!ObjectUtils.isEmpty(processOwnerList)) {
				processOwnerCollection.setRecords(processOwnerList);
				return new ResponseEntity<>(processOwnerCollection, HttpStatus.OK);
			} else
				return new ResponseEntity<>(processOwnerCollection, HttpStatus.NO_CONTENT);
		} catch (Exception e) {
			LOGGER.error("Failed to fetch processOwners with exception {} ", e.getMessage());
			throw e;
		}
	}
	
	private void publishEventMessages(String eventType, ReportVO vo) {
		try {
			String resourceID = vo.getId();
			String reportName = vo.getProductName();
			String publishingUserId = "dna_system";
			String publishingUserName = "";
			String eventMessage = "";
			if (vo.getCreatedBy() != null) {
				publishingUserId = vo.getCreatedBy().getId();
				publishingUserName = vo.getCreatedBy().getFirstName() + " "
						+ vo.getCreatedBy().getLastName();
				if (publishingUserName == null || "".equalsIgnoreCase(publishingUserName))
					publishingUserName = publishingUserId;
			}
			List<String> teamMembersIds = new ArrayList<>();
			List<String> teamMembersEmails = new ArrayList<>();
			for (TeamMemberVO user : vo.getMembers().getReportAdmins()) {
				teamMembersIds.add(user.getShortId());
				teamMembersEmails.add(user.getEmail());
			}
			if ("Dashboard-Report Create".equalsIgnoreCase(eventType)) {
				eventMessage = "Dashboard report " + reportName + " has been created by "
						+ publishingUserName;
				LOGGER.info("Published successfully event {} for report {} with message {}", eventType,
						resourceID, eventMessage);
			}
			if ("Dashboard-Report Delete".equalsIgnoreCase(eventType)) {
				eventMessage = "Dashboard report " + reportName + " has been deleted by "
						+ publishingUserName;
				LOGGER.info("Published successfully event {} for report {} with message {}", eventType,
						resourceID, eventMessage);
			}
			if (eventType != null && eventType != "") {
				kafkaProducer.send(eventType, resourceID, "", publishingUserId, eventMessage, true,
						teamMembersIds, teamMembersEmails, null);
			}
		} catch (Exception e) {
			LOGGER.trace("Failed while publishing Dashboard Report event msg {} ", e.getMessage());
		}
	}
	
	public void notifyAllAdminUsers(String eventType, Long resourceId, String message, String triggeringUser,
			List<ChangeLogVO> changeLogs) {
		LOGGER.debug("Notifying all Admin users on " + eventType + " for " + message);
		UsersCollection usersCollection = dnaAuthClient.getAll();
		List<UserInfoVO> allUsers = usersCollection.getRecords();
		List<String> adminUsersIds = new ArrayList<>();
		List<String> adminUsersEmails = new ArrayList<>();
		for (UserInfoVO user : allUsers) {
			boolean isAdmin = false;
			if (!ObjectUtils.isEmpty(user) && !ObjectUtils.isEmpty(user.getRoles())) {
				isAdmin = user.getRoles().stream().anyMatch(role -> "admin".equalsIgnoreCase(role.getName())|| "admin".equalsIgnoreCase(role.getName()) );
			}
			if (isAdmin) {
				adminUsersIds.add(user.getId());
				adminUsersEmails.add(user.getEmail());
			}
		}
		try {
			String id = resourceId.toString();
			kafkaProducer.send(eventType, id, "", triggeringUser, message, true, adminUsersIds,
					adminUsersEmails, changeLogs);
			LOGGER.info("Successfully notified all admin users for event {} for {} ", eventType, message);
		} catch (Exception e) {
			LOGGER.error("Exception occurred while notifying all Admin users on {}  for {} . Failed with exception {}",
					eventType, message, e.getMessage());
		}
	}
	
	/**
	 * Simple GSON based json objects compare and difference provider
	 * 
	 * @param request
	 * @param existing
	 * @param currentUser
	 * @return
	 */
	public List<ChangeLogVO> jsonObjectCompare(Object request, Object existing, CreatedByVO currentUser) {
		Gson gson = new Gson();
		Type type = new TypeToken<Map<String, Object>>() {
		}.getType();
		Map<String, Object> leftMap = gson.fromJson(gson.toJson(existing), type);
		Map<String, Object> rightMap = gson.fromJson(gson.toJson(request), type);

		Map<String, Object> leftFlatMap = BaseReportService.flatten(leftMap);
		Map<String, Object> rightFlatMap = BaseReportService.flatten(rightMap);

		MapDifference<String, Object> difference = Maps.difference(leftFlatMap, rightFlatMap);

		com.daimler.data.dto.solution.TeamMemberVO teamMemberVO = new com.daimler.data.dto.solution.TeamMemberVO();
		BeanUtils.copyProperties(currentUser, teamMemberVO);
		teamMemberVO.setShortId(currentUser.getId());
		Date changeDate = new Date();

		List<ChangeLogVO> changeLogsVO = new ArrayList<ChangeLogVO>();
		ChangeLogVO changeLogVO = null;
		// Checking for Removed values
		if (null != difference.entriesOnlyOnLeft() && !difference.entriesOnlyOnLeft().isEmpty()) {
			for (Entry<String, Object> entry : difference.entriesOnlyOnLeft().entrySet()) {
				if (!(entry.getKey().toString().contains(ConstantsUtility.CHANGE_LOGS)
						|| entry.getKey().toString().contains(ConstantsUtility.VALUE_CALCULATOR)
						|| entry.getKey().toString().contains(ConstantsUtility.ID))) {
					changeLogVO = new ChangeLogVO();
					changeLogVO.setModifiedBy(teamMemberVO);
					changeLogVO.setChangeDate(changeDate);
					changeLogVO.setFieldChanged(entry.getKey());
					changeLogVO.setOldValue(entry.getValue().toString());
					// setting change Description Starts
					changeLogVO.setChangeDescription(
							toChangeDescription(entry.getKey(), entry.getValue().toString(), null));
					changeLogsVO.add(changeLogVO);
				}
			}
		}
		// Checking for Added values
		if (null != difference.entriesOnlyOnRight() && !difference.entriesOnlyOnRight().isEmpty()) {
			for (Entry<String, Object> entry : difference.entriesOnlyOnRight().entrySet()) {
				if (!(entry.getKey().toString().contains(ConstantsUtility.CHANGE_LOGS)
						|| entry.getKey().toString().contains(ConstantsUtility.VALUE_CALCULATOR)
						|| entry.getKey().toString().contains(ConstantsUtility.ID))) {
					changeLogVO = new ChangeLogVO();
					changeLogVO.setModifiedBy(teamMemberVO);
					changeLogVO.setChangeDate(changeDate);
					changeLogVO.setFieldChanged(entry.getKey());
					changeLogVO.setNewValue(entry.getValue().toString());
					// setting change Description
					changeLogVO.setChangeDescription(
							toChangeDescription(entry.getKey(), null, entry.getValue().toString()));
					changeLogsVO.add(changeLogVO);
				}
			}
		}
		// Checking for value differences
		if (null != difference.entriesDiffering() && !difference.entriesDiffering().isEmpty()) {
			for (Entry<String, ValueDifference<Object>> entry : difference.entriesDiffering().entrySet()) {
				if (!(entry.getKey().toString().contains(ConstantsUtility.CHANGE_LOGS)
						|| entry.getKey().toString().contains(ConstantsUtility.VALUE_CALCULATOR)
						|| entry.getKey().toString().contains(ConstantsUtility.ID))) {
					changeLogVO = new ChangeLogVO();
					changeLogVO.setModifiedBy(teamMemberVO);
					changeLogVO.setChangeDate(changeDate);
					changeLogVO.setFieldChanged(entry.getKey());
					changeLogVO.setOldValue(entry.getValue().leftValue().toString());
					changeLogVO.setNewValue(entry.getValue().rightValue().toString());
					// setting change Description
					changeLogVO.setChangeDescription(toChangeDescription(entry.getKey(),
							entry.getValue().leftValue().toString(), entry.getValue().rightValue().toString()));
					changeLogsVO.add(changeLogVO);
				}
			}
		}
		return changeLogsVO;
	}

	/**
	 * flatten the map
	 * 
	 * @param map
	 * @return Map<String, Object>
	 */
	public static Map<String, Object> flatten(Map<String, Object> map) {
		if (null == map || map.isEmpty()) {
			return new HashMap<String, Object>();
		} else {
			return map.entrySet().stream().flatMap(BaseReportService::flatten).collect(LinkedHashMap::new,
					(m, e) -> m.put("/" + e.getKey(), e.getValue()), LinkedHashMap::putAll);
		}
	}

	/**
	 * flatten map entry
	 * 
	 * @param entry
	 * @return
	 */
	private static Stream<Entry<String, Object>> flatten(Entry<String, Object> entry) {

		if (entry == null) {
			return Stream.empty();
		}

		if (entry.getValue() instanceof Map<?, ?>) {
			Map<?, ?> properties = (Map<?, ?>) entry.getValue();
			return properties.entrySet().stream()
					.flatMap(e -> flatten(new SimpleEntry<>(entry.getKey() + "/" + e.getKey(), e.getValue())));
		}

		if (entry.getValue() instanceof List<?>) {
			List<?> list = (List<?>) entry.getValue();
			return IntStream.range(0, list.size())
					.mapToObj(i -> new SimpleEntry<String, Object>(entry.getKey() + "/" + i, list.get(i)))
					.flatMap(BaseReportService::flatten);
		}

		return Stream.of(entry);
	}
	
	private String toHumanReadableFormat(String raw) {
		if (raw != null) {
			String seperated = raw.replaceAll(String.format("%s|%s|%s", "(?<=[A-Z])(?=[A-Z][a-z])",
					"(?<=[^A-Z])(?=[A-Z])", "(?<=[A-Za-z])(?=[^A-Za-z])"), " ");
			String formatted = Character.toUpperCase(seperated.charAt(0)) + seperated.substring(1);
			return formatted;
		} else
			return raw;
	}

	
	/**
	 * toChangeDescription convert given keyString to changeDescription
	 * 
	 * @param keyString
	 * @param fromValue
	 * @param toValue
	 * @return changeDescription
	 */
	private String toChangeDescription(String keyString, String fromValue, String toValue) {
		keyString = keyString.substring(1);
		String[] keySet = keyString.split("/");
		String at = null;
		int indexValue = 0;
		String fieldValue = "";
		StringBuilder changeDescription = new StringBuilder();
		if (keySet.length > 0) {
			fieldValue = ConstantsUtility.staticMap.get(keySet[0]) != null ? ConstantsUtility.staticMap.get(keySet[0])
					: keySet[0];
			fieldValue = toHumanReadableFormat(fieldValue);
			changeDescription.append(fieldValue + ": ");
		}
		boolean flag = false;
		for (int i = (keySet.length - 1), index = keySet.length; i >= 0; i--) {
			if (!keySet[i].matches("[0-9]") && !flag) {
				String keySetField = ConstantsUtility.staticMap.get(keySet[i]) != null
						? ConstantsUtility.staticMap.get(keySet[i])
						: keySet[i];
				changeDescription.append(toHumanReadableFormat(keySetField));
				flag = true;
			} else if (keySet[i].matches("[0-9]")) {
				indexValue = Integer.parseInt(keySet[i]) + 1;
				at = " at index " + String.valueOf(indexValue);
				index = i;
			} else {
				String keySetField = (ConstantsUtility.staticMap.get(keySet[i]) != null
						? ConstantsUtility.staticMap.get(keySet[i])
						: keySet[i]);
				changeDescription.append(" of " + toHumanReadableFormat(keySetField));
			}
			if (StringUtils.hasText(at) && index != i) {
				changeDescription.append(at);
				at = null;
			}

		}
		if (!StringUtils.hasText(fromValue)) {
			changeDescription.append(" `" + toValue + "` added . ");
		} else if (!StringUtils.hasText(toValue)) {
			changeDescription.append(" `" + fromValue + "` removed . ");
		} else {
			changeDescription.append(" changed from `" + fromValue + "` to `" + toValue + "` .");
		}

		return changeDescription.toString();
	}




}
