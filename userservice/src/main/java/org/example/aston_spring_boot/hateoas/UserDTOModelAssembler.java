package org.example.aston_spring_boot.hateoas;

import org.example.aston_spring_boot.controller.UserController;
import org.example.aston_spring_boot.dto.UserDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class UserDTOModelAssembler implements RepresentationModelAssembler<UserDTO, EntityModel<UserDTO>> {

    @Override
    public EntityModel<UserDTO> toModel(UserDTO userDTO) {

        EntityModel<UserDTO> userDTOModel = EntityModel.of(userDTO,
                linkTo(methodOn(UserController.class).getUser(userDTO.getId())).withSelfRel(),
                linkTo(methodOn(UserController.class).getAllUses(null)).withRel("users"));

        return userDTOModel;
    }
}
