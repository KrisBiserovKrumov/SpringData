package hiberspring.service.impl;

import com.google.gson.Gson;
import hiberspring.common.GlobalConstants;
import hiberspring.domain.dtos.EmployeeCardSeedDto;
import hiberspring.domain.entities.EmployeeCard;
import hiberspring.repository.EmployeeCardRepository;
import hiberspring.service.EmployeeCardService;
import hiberspring.util.ValidationUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

@Service
public class EmployeeCardServiceImpl implements EmployeeCardService {

    private  final EmployeeCardRepository employeeCardRepository;
    private final ModelMapper modelMapper;
    private final ValidationUtil validationUtil;
    private final Gson gson;

    @Autowired
    public EmployeeCardServiceImpl(EmployeeCardRepository employeeCardRepository,
                                   ModelMapper modelMapper, ValidationUtil validationUtil,
                                   Gson gson) {
        this.employeeCardRepository = employeeCardRepository;
        this.modelMapper = modelMapper;
        this.validationUtil = validationUtil;
        this.gson = gson;
    }

    @Override
    public Boolean employeeCardsAreImported() {
        return this.employeeCardRepository.count() > 0 ;
    }

    @Override
    public String readEmployeeCardsJsonFile() throws IOException {
        return Files.readString(Path.of(GlobalConstants.EMPLOYEE_CARDS_FILE_PATH));
    }

    @Override
    public String importEmployeeCards(String employeeCardsFileContent) throws FileNotFoundException {

        StringBuilder resultInfo = new StringBuilder();

        EmployeeCardSeedDto[] dtos = this.gson
                .fromJson(new FileReader(GlobalConstants.EMPLOYEE_CARDS_FILE_PATH)
                        ,EmployeeCardSeedDto[].class);

        Arrays.stream(dtos).forEach(employeeCardSeedDto -> {

            if (this.validationUtil.isValid(employeeCardSeedDto)){
              if (this.employeeCardRepository.findByNumber(employeeCardSeedDto.getNumber()) == null){
                  EmployeeCard employeeCard = this.modelMapper.map(employeeCardSeedDto,EmployeeCard.class);

                  this.employeeCardRepository.saveAndFlush(employeeCard);

                  resultInfo.append("Successfully imported Employee Card ");
                  resultInfo.append(employeeCardSeedDto.getNumber());

              }else {
                  resultInfo.append(GlobalConstants.ALREADY_DATA_MESSAGE);
              }
            }else {
                resultInfo.append(GlobalConstants.INCORRECT_DATA_MESSAGE);
            }
            resultInfo.append(System.lineSeparator());
        });

        return resultInfo.toString();
    }

    @Override
    public EmployeeCard getCardByNumber(String number) {
        return this.employeeCardRepository.findByNumber(number);
    }
}
