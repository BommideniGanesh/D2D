export interface NycDriver {
  vehicle_license_number: string;
  name: string;
  license_type: string;
  expiration_date: string;
  vehicle_year?: string | number;
  vehicle_make?: string;
  vehicle_model?: string;
  veh?: string;
  base_name?: string;
  base_number?: string;
  base_type?: string;
  dmv_license_plate_number?: string;
  for_hire_status?: string;
  wheelchair_accessible?: string;
  permit_license_number?: string;
  active?: string;
}
