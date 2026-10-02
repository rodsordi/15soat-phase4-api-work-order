package br.com.fiap.phase4.workorder.domain.port.in;

import br.com.fiap.phase4.workorder.domain.model.Material;

import java.util.List;

public interface ListMaterialsUseCase {
    List<Material> listAll();
}
