package org.example.aston_spring_boot.mapper;

import org.example.aston_spring_boot.dto.UserCreateDTO;
import org.example.aston_spring_boot.dto.UserDTO;
import org.example.aston_spring_boot.dto.UserUpdateDTO;
import org.example.aston_spring_boot.model.User;
import org.mapstruct.*;
import org.springframework.stereotype.Component;

import java.util.List;


@Mapper(
        uses = {JsonNullableMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
@Component
public interface UserMapper {
    User map(UserCreateDTO dto);
    UserDTO map(User user);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    User update(UserUpdateDTO dto, @MappingTarget User user);
    List<UserDTO> fromUsers(List<User> users);
}
