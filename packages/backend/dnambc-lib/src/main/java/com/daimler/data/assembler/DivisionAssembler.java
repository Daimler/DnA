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

import com.daimler.data.db.entities.DivisionNsql;
import com.daimler.data.db.jsonb.Division;
import com.daimler.data.db.jsonb.SubDivision;
import com.daimler.data.dto.divisions.DivisionVO;
import com.daimler.data.dto.divisions.SubdivisionVO;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class DivisionAssembler implements GenericAssembler<DivisionVO, DivisionNsql> {

	@Override
	public DivisionVO toVo(DivisionNsql entity) {
		DivisionVO divisionVO = null;
		if (Objects.nonNull(entity)) {
			divisionVO = new DivisionVO();
			divisionVO.setId(entity.getId());
			divisionVO.setName(entity.getData().getName());
			List<SubdivisionVO> subdivisionVOs = new ArrayList<SubdivisionVO>();
			if (null != entity.getData().getSubdivisions() && !entity.getData().getSubdivisions().isEmpty()) {
				for (SubDivision subDivision : entity.getData().getSubdivisions()) {
					SubdivisionVO vo = new SubdivisionVO();
					BeanUtils.copyProperties(subDivision, vo);
					subdivisionVOs.add(vo);
				}
			}
			divisionVO.setSubdivisions(subdivisionVOs);
		}
		return divisionVO;
	}

	@Override
	public DivisionNsql toEntity(DivisionVO vo) {
		DivisionNsql divisionNsql = null;
		if (Objects.nonNull(vo)) {
			divisionNsql = new DivisionNsql();
			Division division = new Division();
			division.setName(vo.getName());
			divisionNsql.setData(division);
			if (vo.getId() != null)
				divisionNsql.setId(vo.getId());
		}
		return divisionNsql;
	}

	public List<SubdivisionVO> toSubDivisionVoList(DivisionNsql entity) {
		List<SubdivisionVO> subdivisionsVo = new ArrayList<>();
		if (Objects.nonNull(entity)) {
			Division division = entity.getData();
			if (division != null) {
				List<SubDivision> subDivisions = division.getSubdivisions();
				if (subDivisions != null && !subDivisions.isEmpty())
					subdivisionsVo = subDivisions.stream().map(n -> this.toSubDivisionVo(n))
							.collect(Collectors.toList());
			}
		}
		return subdivisionsVo;

	}

	public SubdivisionVO toSubDivisionVo(SubDivision subdivision) {
		SubdivisionVO vo = null;
		if (subdivision != null) {
			vo = new SubdivisionVO();
			vo.setId(subdivision.getId());
			vo.setName(subdivision.getName());
		}
		return vo;
	}

	public SubDivision toSubDivision(SubdivisionVO subdivisionVo) {
		SubDivision subdivision = null;
		if (subdivisionVo != null) {
			subdivision = new SubDivision();
			subdivision.setId(subdivisionVo.getId());
			subdivision.setName(subdivisionVo.getName());
		}
		return subdivision;
	}

}
