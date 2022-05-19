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

package com.daimler.data.assembler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.daimler.data.db.entities.ReportNsql;
import com.daimler.data.db.jsonb.report.CreatedBy;
import com.daimler.data.db.jsonb.report.Customer;
import com.daimler.data.db.jsonb.report.CustomerDetails;
import com.daimler.data.db.jsonb.report.DataWarehouse;
import com.daimler.data.db.jsonb.report.Description;
import com.daimler.data.db.jsonb.report.Division;
import com.daimler.data.db.jsonb.report.KPI;
import com.daimler.data.db.jsonb.report.Member;
import com.daimler.data.db.jsonb.report.Report;
import com.daimler.data.db.jsonb.report.SingleDataSource;
import com.daimler.data.db.jsonb.report.Subdivision;
import com.daimler.data.db.jsonb.report.TeamMember;
import com.daimler.data.dto.report.CreatedByVO;
import com.daimler.data.dto.report.CustomerDetailsVO;
import com.daimler.data.dto.report.CustomerVO;
import com.daimler.data.dto.report.DataAndFunctionVO;
import com.daimler.data.dto.report.DataWarehouseVO;
import com.daimler.data.dto.report.DescriptionVO;
import com.daimler.data.dto.report.DivisionVO;
import com.daimler.data.dto.report.KPIVO;
import com.daimler.data.dto.report.MemberVO;
import com.daimler.data.dto.report.ReportVO;
import com.daimler.data.dto.report.SingleDataSourceVO;
import com.daimler.data.dto.report.SubdivisionVO;
import com.daimler.data.dto.report.TeamMemberVO;
import com.daimler.data.dto.report.TeamMemberVO.UserTypeEnum;

@Component
public class ReportAssembler implements GenericAssembler<ReportVO, ReportNsql> {

	@Override
	public ReportVO toVo(ReportNsql entity) {
		ReportVO vo = null;
		if (entity != null && entity.getData() != null) {
			vo = new ReportVO();
			Report report = entity.getData();
			BeanUtils.copyProperties(report, vo);
			if (report.getCreatedBy() != null) {
				vo.setCreatedBy(new CreatedByVO().id(report.getCreatedBy().getId())
						.firstName(report.getCreatedBy().getFirstName()).lastName(report.getCreatedBy().getLastName())
						.department(report.getCreatedBy().getDepartment()).email(report.getCreatedBy().getEmail())
						.department(report.getCreatedBy().getDepartment()));
			}
			if (report.getDescription() != null) {
				DescriptionVO descriptionVO = new DescriptionVO();
				BeanUtils.copyProperties(report.getDescription(), descriptionVO);
				DivisionVO divisionvo = new DivisionVO();
				Division division = report.getDescription().getDivision();
				if (division != null) {
					BeanUtils.copyProperties(division, divisionvo);
					SubdivisionVO subdivisionVO = new SubdivisionVO();
					if (division.getSubdivision() != null)
						BeanUtils.copyProperties(division.getSubdivision(), subdivisionVO);
					divisionvo.setSubdivision(subdivisionVO);
					descriptionVO.setDivision(divisionvo);
				}
				vo.setDescription(descriptionVO);
			}
			if (report.getCustomer() != null) {
				CustomerVO customerVO = new CustomerVO();
				BeanUtils.copyProperties(report.getCustomer(), customerVO);
				if (!ObjectUtils.isEmpty(report.getCustomer().getCustomers())) {
					List<CustomerDetailsVO> customerDetails = report.getCustomer().getCustomers().stream()
							.map(n -> toCustomerDetailsVO(n)).collect(Collectors.toList());
					customerVO.setCustomerDetails(customerDetails);
				}
				if (!ObjectUtils.isEmpty(report.getCustomer().getProcessOwners())) {
					List<TeamMemberVO> processOwners = report.getCustomer().getProcessOwners().stream()
							.map(n -> toTeamMemberVO(n)).collect(Collectors.toList());
					customerVO.setProcessOwners(processOwners);
				}

				vo.setCustomer(customerVO);
			}
			if (!ObjectUtils.isEmpty(report.getKpis())) {
				List<KPIVO> kpis = new ArrayList<KPIVO>();
				kpis = report.getKpis().stream().map(n -> toKPIVO(n)).collect(Collectors.toList());
				vo.setKpis(kpis);
			}
			DataAndFunctionVO dataAndFunctionVO = new DataAndFunctionVO();
			if (!ObjectUtils.isEmpty(report.getDataWarehouses())) {
				List<DataWarehouseVO> dataWarehouseVO = new ArrayList<DataWarehouseVO>();
				dataWarehouseVO = report.getDataWarehouses().stream().map(n -> toDataWarehouseVO(n))
						.collect(Collectors.toList());
				dataAndFunctionVO.setDataWarehouseInUse(dataWarehouseVO);
			}

			if (!ObjectUtils.isEmpty(report.getSingleDataSources())) {
				List<SingleDataSourceVO> singleDataSourcesVO = new ArrayList<SingleDataSourceVO>();
				singleDataSourcesVO = report.getSingleDataSources().stream().map(n -> toSingleDataSourceVO(n))
						.collect(Collectors.toList());
				dataAndFunctionVO.setSingleDataSources(singleDataSourcesVO);
			}
			vo.setDataAndFunctions(dataAndFunctionVO);
			if (report.getMember() != null) {
				MemberVO memberVO = new MemberVO();
				BeanUtils.copyProperties(report.getMember(), memberVO);
				if (!ObjectUtils.isEmpty(report.getMember().getDevelopers())) {
					List<TeamMemberVO> developers = report.getMember().getDevelopers().stream()
							.map(n -> toTeamMemberVO(n)).collect(Collectors.toList());
					memberVO.setDevelopers(developers);
				}
				if (!ObjectUtils.isEmpty(report.getMember().getProductOwners())) {
					List<TeamMemberVO> productOwners = report.getMember().getProductOwners().stream()
							.map(n -> toTeamMemberVO(n)).collect(Collectors.toList());
					memberVO.setProductOwners(productOwners);
				}
				if (!ObjectUtils.isEmpty(report.getMember().getAdmin())) {
					List<TeamMemberVO> admin = report.getMember().getAdmin().stream().map(n -> toTeamMemberVO(n))
							.collect(Collectors.toList());
					memberVO.setAdmin(admin);
				}
				vo.setMembers(memberVO);
			}
			if (!ObjectUtils.isEmpty(report.getOpenSegments())) {
				List<ReportVO.OpenSegmentsEnum> openSegmentsEnumList = new ArrayList<>();
				report.getOpenSegments().forEach(
						openSegment -> openSegmentsEnumList.add(ReportVO.OpenSegmentsEnum.valueOf(openSegment)));
				vo.setOpenSegments(openSegmentsEnumList);
			}
			vo.setId(entity.getId());
		}

		return vo;
	}

	private DataWarehouseVO toDataWarehouseVO(DataWarehouse dataWarehouse) {
		DataWarehouseVO vo = null;
		if (dataWarehouse != null) {
			vo = new DataWarehouseVO();
			BeanUtils.copyProperties(dataWarehouse, vo);
		}
		return vo;
	}

	private SingleDataSourceVO toSingleDataSourceVO(SingleDataSource singleDataSource) {
		SingleDataSourceVO vo = null;
		if (singleDataSource != null) {
			vo = new SingleDataSourceVO();
			BeanUtils.copyProperties(singleDataSource, vo);
		}
		return vo;
	}

	private KPIVO toKPIVO(KPI kpi) {
		KPIVO kPIVO = null;
		if (kpi != null) {
			kPIVO = new KPIVO();
			BeanUtils.copyProperties(kpi, kPIVO);
		}
		return kPIVO;
	}

	private CustomerDetailsVO toCustomerDetailsVO(CustomerDetails customerDetails) {
		CustomerDetailsVO vo = null;
		if (customerDetails != null) {
			vo = new CustomerDetailsVO();
			BeanUtils.copyProperties(customerDetails, vo);
		}
		return vo;
	}

	private TeamMemberVO toTeamMemberVO(TeamMember teamMember) {
		TeamMemberVO vo = null;
		if (teamMember != null) {
			vo = new TeamMemberVO();
			BeanUtils.copyProperties(teamMember, vo);
			if (StringUtils.hasText(teamMember.getUserType())) {
				vo.setUserType(UserTypeEnum.valueOf(teamMember.getUserType()));
			}
		}
		return vo;
	}

	@Override
	public ReportNsql toEntity(ReportVO vo) {
		ReportNsql entity = null;
		if (vo != null) {
			entity = new ReportNsql();
			String id = vo.getId();
			if (id != null && !id.isEmpty() && !id.trim().isEmpty()) {
				entity.setId(id);
			}
			Report report = new Report();
			BeanUtils.copyProperties(vo, report);
			report.setPublish(vo.isPublish());
			if (vo.getCreatedBy() != null) {
				report.setCreatedBy(new CreatedBy(vo.getCreatedBy().getId(), vo.getCreatedBy().getFirstName(),
						vo.getCreatedBy().getLastName(), vo.getCreatedBy().getDepartment(),
						vo.getCreatedBy().getEmail(), vo.getCreatedBy().getMobileNumber()));
			}

			if (vo.getDescription() != null) {
				Description description = new Description();
				BeanUtils.copyProperties(vo.getDescription(), description);
				DivisionVO divisionvo = vo.getDescription().getDivision();
				Division division = new Division();
				if (divisionvo != null) {
					BeanUtils.copyProperties(divisionvo, division);
					Subdivision subdivision = new Subdivision();
					if (divisionvo.getSubdivision() != null)
						BeanUtils.copyProperties(divisionvo.getSubdivision(), subdivision);
					division.setSubdivision(subdivision);
					description.setDivision(division);
				}
				report.setDescription(description);
			}

			if (vo.getCustomer() != null) {
				Customer customer = new Customer();
				BeanUtils.copyProperties(vo.getCustomer(), customer);
				if (!ObjectUtils.isEmpty(vo.getCustomer().getCustomerDetails())) {
					List<CustomerDetails> customers = vo.getCustomer().getCustomerDetails().stream()
							.map(n -> toCustomerDetailsJson(n)).collect(Collectors.toList());
					customer.setCustomers(customers);
				}
				if (!ObjectUtils.isEmpty(vo.getCustomer().getProcessOwners())) {
					List<TeamMember> processOwners = vo.getCustomer().getProcessOwners().stream()
							.map(n -> toTeamMemberJson(n)).collect(Collectors.toList());
					customer.setProcessOwners(processOwners);
				}
				report.setCustomer(customer);
			}
			if (!ObjectUtils.isEmpty(vo.getKpis())) {
				List<KPI> kpis = new ArrayList<KPI>();
				kpis = vo.getKpis().stream().map(n -> toKPIJson(n)).collect(Collectors.toList());
				report.setKpis(kpis);
			}
			if (Objects.nonNull(vo.getDataAndFunctions())
					&& !ObjectUtils.isEmpty(vo.getDataAndFunctions().getDataWarehouseInUse())) {
				List<DataWarehouse> dataWarehouseInUse = new ArrayList<DataWarehouse>();
				dataWarehouseInUse = vo.getDataAndFunctions().getDataWarehouseInUse().stream()
						.map(n -> toDataWarehouseJson(n)).collect(Collectors.toList());
				report.setDataWarehouses(dataWarehouseInUse);
			}

			if (Objects.nonNull(vo.getDataAndFunctions())
					&& !ObjectUtils.isEmpty(vo.getDataAndFunctions().getSingleDataSources())) {
				List<SingleDataSource> singleDataSources = new ArrayList<SingleDataSource>();
				singleDataSources = vo.getDataAndFunctions().getSingleDataSources().stream()
						.map(n -> toSingleDataSourceJson(n)).collect(Collectors.toList());
				report.setSingleDataSources(singleDataSources);
			}

			if (vo.getMembers() != null) {
				Member member = new Member();
				BeanUtils.copyProperties(vo.getMembers(), member);
				if (!ObjectUtils.isEmpty(vo.getMembers().getDevelopers())) {
					List<TeamMember> developers = vo.getMembers().getDevelopers().stream().map(n -> toTeamMemberJson(n))
							.collect(Collectors.toList());
					member.setDevelopers(developers);
				}
				if (!ObjectUtils.isEmpty(vo.getMembers().getProductOwners())) {
					List<TeamMember> productOwners = vo.getMembers().getProductOwners().stream()
							.map(n -> toTeamMemberJson(n)).collect(Collectors.toList());
					member.setProductOwners(productOwners);
				}
				if (!ObjectUtils.isEmpty(vo.getMembers().getAdmin())) {
					List<TeamMember> admin = vo.getMembers().getAdmin().stream().map(n -> toTeamMemberJson(n))
							.collect(Collectors.toList());
					member.setAdmin(admin);
				}
				report.setMember(member);
			}
			if (!ObjectUtils.isEmpty(vo.getOpenSegments())) {
				List<String> openSegmentList = new ArrayList<>();
				vo.getOpenSegments().forEach(openSegmentsEnum -> {
					openSegmentList.add(openSegmentsEnum.name());
				});
				report.setOpenSegments(openSegmentList);
			}
			entity.setData(report);
		}

		return entity;
	}

	private DataWarehouse toDataWarehouseJson(DataWarehouseVO vo) {
		DataWarehouse dataWarehouse = null;
		if (vo != null) {
			dataWarehouse = new DataWarehouse();
			BeanUtils.copyProperties(vo, dataWarehouse);
		}
		return dataWarehouse;
	}

	private SingleDataSource toSingleDataSourceJson(SingleDataSourceVO vo) {
		SingleDataSource singleDataSource = null;
		if (vo != null) {
			singleDataSource = new SingleDataSource();
			BeanUtils.copyProperties(vo, singleDataSource);
		}
		return singleDataSource;
	}

	private KPI toKPIJson(KPIVO vo) {
		KPI kpi = null;
		if (vo != null) {
			kpi = new KPI();
			BeanUtils.copyProperties(vo, kpi);
		}
		return kpi;
	}

	private CustomerDetails toCustomerDetailsJson(CustomerDetailsVO vo) {
		CustomerDetails customerDetails = null;
		if (vo != null) {
			customerDetails = new CustomerDetails();
			BeanUtils.copyProperties(vo, customerDetails);
		}
		return customerDetails;
	}

	private TeamMember toTeamMemberJson(TeamMemberVO vo) {
		TeamMember teamMember = null;
		if (vo != null) {
			teamMember = new TeamMember();
			BeanUtils.copyProperties(vo, teamMember);
			if (vo.getUserType() != null) {
				teamMember.setUserType(vo.getUserType().name());
			}
		}
		return teamMember;
	}

	public List<String> toList(String parameter) {
		List<String> results = null;
		if (StringUtils.hasText(parameter)) {
			results = new ArrayList<String>();
			String[] parameters = parameter.split(",");
			if (!ObjectUtils.isEmpty(parameters))
				results = Arrays.asList(parameters);
		}
		return results;
	}
}
