<?php

class Constants
{
    //DATABASE DETAILS
    static $DB_SERVER="localhost";
    static $DB_NAME="spiritualteachersdb";
    static $USERNAME="daisy";
    static $PASSWORD="kimali1234";

    //STATEMENTS
    static $SQL_SELECT_ALL="SELECT * FROM spiritualteacherstb";
}

class Spirituality
{
    /*
    1. Connect to database
    2. Return connection object
    */

    public function connect()
    {
        $con = new mysqli(Constants::$DB_SERVER,Constants::$USERNAME,Constants::$PASSWORD,Constants::$DB_NAME);
        if ($con -> connect_error) {
            return null;
        }
        else{
            return $con;
        }
    }
    public function insert()
    {
        //INSERT DATA TO MYSQLI DATABASE
        $con = $this -> connect();
        if ($con != null) 
        {
            // Get image name
            $image_name = $_FILES['image']['name'];
            // Get text
            $teacher_name = mysqli_real_escape_string($con, $_POST['teacher_name']);
            $teacher_description = mysqli_real_escape_string($con, $_POST['teacher_description']);

            //Image file directory
            $target = "images/".basename($image_name);
            $sql = "INSERT INTO spiritualteacherstb(teacher_image_url, teacher_name, teacher_description)
                    VALUES('$image_name', '$teacher_name', '$teacher_description')";
            try
            {
                $result = $con -> query($sql);
                if($result)
                {
                    if(move_uploaded_file($_FILES['image']['tmp_name'], $target))
                    {
                        print(json_encode(array("message" => "success")));
                    } else {
                        print(json_encode(array("message" => "Saved but unable to move image to appropriate folder")));
                    }
                }
                else
                {
                    print(json_encode(array("message" => "Unsuccessful. Connection was successful but data could not be inserted.")));
                }
                $con -> close();
            }  
            catch(Exception $e) 
            {
                print(json_encode(array("message" => "ERROR PHP EXCEPTION : CAN'T SAVE TO MYSQL".$e -> getMessage())));
                $con -> close();
            }     
        }
        else
        {
            print(json_encode(array("message" => "ERROR PHP EXCEPTION : CAN'T CONNECT TO MYSQL. NULL CONNECTION.")));
        }
    }

    public function select()
    {
        $con = $this -> connect();
        if($con != null)
        {
            $result = $con->query(Constants::$SQL_SELECT_ALL);
            if($result->num_rows > 0)
            {
                $spiritual_teachers=array();
                while($row=$result->fetch_array())
                {
                    array_push($spiritual_teachers, array("id"=>$row['id'], "teacher_name"=>$row['teacher_name'],
                    "teacher_description"=>$row['teacher_description'],"teacher_image_url"=>$row['teacher_image_url']));
                }
                print(json_encode(array_reverse($spiritual_teachers)));
            }else
            {

            }
            $con->close();
        }
        else
        {
            print(json_encode(array("message" => "PHP EXCEPTION : CAN'T CONNECT TO MYSQL. NULL CONNECTION.")));
        }
    }

    public function handleRequest()
    {
        if(isset($_POST['name'])){
            $this->insert();
        }
        else {
            $this->select();
        }
    }
}
$spirituality = new Spirituality();
$spirituality->handleRequest();

?>